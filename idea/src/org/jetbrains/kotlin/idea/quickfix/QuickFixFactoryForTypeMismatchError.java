/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.quickfix;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import kotlin.Function1;
import kotlin.KotlinPackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.builtins.KotlinBuiltIns;
import org.jetbrains.kotlin.descriptors.CallableDescriptor;
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor;
import org.jetbrains.kotlin.diagnostics.Diagnostic;
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters1;
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters2;
import org.jetbrains.kotlin.diagnostics.Errors;
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages;
import org.jetbrains.kotlin.idea.caches.resolve.ResolvePackage;
import org.jetbrains.kotlin.idea.imports.ImportsPackage;
import org.jetbrains.kotlin.idea.util.UtilPackage;
import org.jetbrains.kotlin.psi.*;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.bindingContextUtil.BindingContextUtilPackage;
import org.jetbrains.kotlin.resolve.calls.callUtil.CallUtilPackage;
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall;
import org.jetbrains.kotlin.resolve.scopes.JetScope;
import org.jetbrains.kotlin.resolve.scopes.JetScopeUtils;
import org.jetbrains.kotlin.types.JetType;
import org.jetbrains.kotlin.types.TypeUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

//TODO: should use change signature to deal with cases of multiple overridden descriptors
public class QuickFixFactoryForTypeMismatchError extends JetIntentionActionsFactory {
    private final static Logger LOG = Logger.getInstance(QuickFixFactoryForTypeMismatchError.class);

    private static boolean isResolvableType(@NotNull JetType type, @Nullable JetScope scope) {
        if (ImportsPackage.canBeReferencedViaImport(type)) return true;

        ClassifierDescriptor descriptor = type.getConstructor().getDeclarationDescriptor();
        if (descriptor == null || descriptor.getName().isSpecial()) return false;

        return scope != null && scope.getClassifier(descriptor.getName()) == descriptor;
    }

    private static JetType approximateWithResolvableType(@NotNull JetType type, @Nullable final JetScope scope) {
        if (isResolvableType(type, scope)) return type;
        JetType superType = KotlinPackage.firstOrNull(
                TypeUtils.getAllSupertypes(type),
                new Function1<JetType, Boolean>() {
                    @Override
                    public Boolean invoke(JetType type) {
                        return isResolvableType(type, scope);
                    }
                }
        );
        return superType != null ? superType : KotlinBuiltIns.getInstance().getAnyType();
    }

    @NotNull
    @Override
    protected List<IntentionAction> doCreateActions(@NotNull Diagnostic diagnostic) {
        List<IntentionAction> actions = new LinkedList<IntentionAction>();

        AnalysisResult analysisResult = ResolvePackage.analyzeFullyAndGetResult((JetFile) diagnostic.getPsiFile());
        BindingContext context = analysisResult.getBindingContext();

        PsiElement diagnosticElement = diagnostic.getPsiElement();
        if (!(diagnosticElement instanceof JetExpression)) {
            LOG.error("Unexpected element: " + diagnosticElement.getText());
            return Collections.emptyList();
        }

        JetExpression expression = (JetExpression) diagnosticElement;

        JetType expectedType;
        JetType expressionType;
        if (diagnostic.getFactory() == Errors.TYPE_MISMATCH) {
            DiagnosticWithParameters2<JetExpression, JetType, JetType> diagnosticWithParameters = Errors.TYPE_MISMATCH.cast(diagnostic);
            expectedType = diagnosticWithParameters.getA();
            expressionType = diagnosticWithParameters.getB();
        }
        else if (diagnostic.getFactory() == Errors.NULL_FOR_NONNULL_TYPE) {
            DiagnosticWithParameters1<JetConstantExpression, JetType> diagnosticWithParameters =
                    Errors.NULL_FOR_NONNULL_TYPE.cast(diagnostic);
            expectedType = diagnosticWithParameters.getA();
            expressionType = UtilPackage.makeNullable(expectedType);
        }
        else if (diagnostic.getFactory() == Errors.CONSTANT_EXPECTED_TYPE_MISMATCH) {
            DiagnosticWithParameters2<JetConstantExpression, String, JetType> diagnosticWithParameters =
                    Errors.CONSTANT_EXPECTED_TYPE_MISMATCH.cast(diagnostic);
            expectedType = diagnosticWithParameters.getB();
            expressionType = context.get(BindingContext.EXPRESSION_TYPE, expression);
            if (expressionType == null) {
                LOG.error("No type inferred: " + expression.getText());
                return Collections.emptyList();
            }
        }
        else {
            LOG.error("Unexpected diagnostic: " + DefaultErrorMessages.render(diagnostic));
            return Collections.emptyList();
        }

        // We don't want to cast a cast or type-asserted expression:
        if (!(expression instanceof JetBinaryExpressionWithTypeRHS) && !(expression.getParent() instanceof  JetBinaryExpressionWithTypeRHS)) {
            actions.add(new CastExpressionFix(expression, expectedType));
        }

        // Property initializer type mismatch property type:
        JetProperty property = PsiTreeUtil.getParentOfType(expression, JetProperty.class);
        if (property != null) {
            JetPropertyAccessor getter = property.getGetter();
            JetExpression initializer = property.getInitializer();
            if (QuickFixUtil.canEvaluateTo(initializer, expression) ||
                (getter != null && QuickFixUtil.canFunctionOrGetterReturnExpression(property.getGetter(), expression))) {
                JetScope scope = JetScopeUtils.getResolutionScope(property, analysisResult);
                JetType typeToInsert = approximateWithResolvableType(expressionType, scope);
                actions.add(new ChangeVariableTypeFix(property, typeToInsert));
            }
        }

        PsiElement expressionParent = expression.getParent();

        // Mismatch in returned expression:

        JetDeclaration function = expressionParent instanceof JetReturnExpression
                                  ? BindingContextUtilPackage.getTargetDeclaration((JetReturnExpression) expressionParent, context)
                                  : PsiTreeUtil.getParentOfType(expression, JetFunction.class, true);
        if (function instanceof JetFunction && QuickFixUtil.canFunctionOrGetterReturnExpression(function, expression)) {
            JetScope scope = JetScopeUtils.getResolutionScope(function, analysisResult);
            JetType typeToInsert = approximateWithResolvableType(expressionType, scope);
            actions.add(new ChangeFunctionReturnTypeFix((JetFunction) function, typeToInsert));
        }

        // Fixing overloaded operators:
        if (expression instanceof JetOperationExpression) {
            ResolvedCall<?> resolvedCall = CallUtilPackage.getResolvedCall(expression, context);
            if (resolvedCall != null) {
                JetFunction declaration = getFunctionDeclaration(resolvedCall);
                if (declaration != null) {
                    actions.add(new ChangeFunctionReturnTypeFix(declaration, expectedType));
                }
            }
        }

        // Change function return type when TYPE_MISMATCH is reported on call expression:
        if (expression instanceof JetCallExpression) {
            ResolvedCall<?> resolvedCall = CallUtilPackage.getResolvedCall(expression, context);
            if (resolvedCall != null) {
                JetFunction declaration = getFunctionDeclaration(resolvedCall);
                if (declaration != null) {
                    actions.add(new ChangeFunctionReturnTypeFix(declaration, expectedType));
                }
            }
        }

        ResolvedCall<? extends CallableDescriptor> resolvedCall = CallUtilPackage.getParentResolvedCall(expression, context, true);
        if (resolvedCall != null) {
            // to fix 'type mismatch' on 'if' branches
            // todo: the same with 'when'
            JetExpression parentIf = QuickFixUtil.getParentIfForBranch(expression);
            JetExpression argumentExpression = (parentIf != null) ? parentIf : expression;
            ValueArgument valueArgument = CallUtilPackage.getValueArgumentForExpression(resolvedCall.getCall(), argumentExpression);
            if (valueArgument != null) {
                JetParameter correspondingParameter = QuickFixUtil.getParameterDeclarationForValueArgument(resolvedCall, valueArgument);
                JetType valueArgumentType = diagnostic.getFactory() == Errors.NULL_FOR_NONNULL_TYPE
                                            ? expressionType
                                            : context.get(BindingContext.EXPRESSION_TYPE, valueArgument.getArgumentExpression());
                if (correspondingParameter != null && valueArgumentType != null) {
                    JetScope scope = JetScopeUtils.getResolutionScope(valueArgument.getArgumentExpression(), analysisResult);
                    JetType typeToInsert = approximateWithResolvableType(valueArgumentType, scope);
                    actions.add(new ChangeParameterTypeFix(correspondingParameter, typeToInsert));
                }
            }
        }
        return actions;
    }

    @Nullable
    private static JetFunction getFunctionDeclaration(@NotNull ResolvedCall<?> resolvedCall) {
        PsiElement result = QuickFixUtil.safeGetDeclaration(resolvedCall.getResultingDescriptor());
        if (result instanceof JetFunction) {
            return (JetFunction) result;
        }
        return null;
    }
}
