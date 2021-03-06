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

package org.jetbrains.kotlin.idea.completion.handlers

import com.intellij.openapi.command.CommandProcessor
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.openapi.util.TextRange
import com.intellij.codeInsight.template.Template
import org.jetbrains.kotlin.idea.refactoring.JetNameSuggester
import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.ExpressionContext
import com.intellij.codeInsight.template.TextResult
import com.intellij.codeInsight.template.Result
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import org.jetbrains.kotlin.types.JetType
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.JetExpression
import org.jetbrains.kotlin.idea.completion.ExpectedInfos
import org.jetbrains.kotlin.psi.JetFile
import org.jetbrains.kotlin.idea.caches.resolve.getResolutionFacade
import org.jetbrains.kotlin.idea.util.application.runWriteAction
import org.jetbrains.kotlin.idea.refactoring.EmptyValidator
import org.jetbrains.kotlin.idea.util.IdeDescriptorRenderers
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

fun insertLambdaTemplate(context: InsertionContext, placeholderRange: TextRange, lambdaType: JetType) {
    val explicitParameterTypes = needExplicitParameterTypes(context, placeholderRange, lambdaType)

    // we start template later to not interfere with insertion of tail type
    val commandProcessor = CommandProcessor.getInstance()
    val commandName = commandProcessor.getCurrentCommandName()
    val commandGroupId = commandProcessor.getCurrentCommandGroupId()

    val rangeMarker = context.getDocument().createRangeMarker(placeholderRange)

    context.setLaterRunnable {
        commandProcessor.executeCommand(context.getProject(), {
            runWriteAction {
                try {
                    if (rangeMarker.isValid()) {
                        context.getDocument().deleteString(rangeMarker.getStartOffset(), rangeMarker.getEndOffset())
                        context.getEditor().getCaretModel().moveToOffset(rangeMarker.getStartOffset())
                        val template = buildTemplate(lambdaType, explicitParameterTypes, context.getProject())
                        TemplateManager.getInstance(context.getProject()).startTemplate(context.getEditor(), template)
                    }
                }
                finally {
                    rangeMarker.dispose()
                }
            }
        }, commandName, commandGroupId)
    }
}

fun buildLambdaPresentation(lambdaType: JetType): String {
    val parameterTypes = functionParameterTypes(lambdaType)
    val parametersPresentation = parameterTypes.map { IdeDescriptorRenderers.SOURCE_CODE_SHORT_NAMES_IN_TYPES.renderType(it) }.joinToString(", ")
    fun wrap(s: String) = if (parameterTypes.size() != 1) "($s)" else s
    return "{ ${wrap(parametersPresentation)} -> ... }"
}

private fun needExplicitParameterTypes(context: InsertionContext, placeholderRange: TextRange, lambdaType: JetType): Boolean {
    PsiDocumentManager.getInstance(context.getProject()).commitAllDocuments()
    val file = context.getFile() as JetFile
    val expression = PsiTreeUtil.findElementOfClassAtRange(file, placeholderRange.getStartOffset(), placeholderRange.getEndOffset(), javaClass<JetExpression>())
    if (expression == null) return false

    val resolutionFacade = file.getResolutionFacade()
    val bindingContext = resolutionFacade.analyze(expression, BodyResolveMode.PARTIAL)
    val expectedInfos = ExpectedInfos(bindingContext, resolutionFacade, resolutionFacade.findModuleDescriptor(file), false).calculate(expression) ?: return false
    val functionTypes = expectedInfos.map { it.type }.filter { KotlinBuiltIns.isExactFunctionOrExtensionFunctionType(it) }.toSet()
    if (functionTypes.size() <= 1) return false

    val lambdaParameterCount = KotlinBuiltIns.getParameterTypeProjectionsFromFunctionType(lambdaType).size()
    return functionTypes.filter { KotlinBuiltIns.getParameterTypeProjectionsFromFunctionType(it).size() == lambdaParameterCount }.size() > 1
}

private fun buildTemplate(lambdaType: JetType, explicitParameterTypes: Boolean, project: Project): Template {
    val parameterTypes = functionParameterTypes(lambdaType)

    val useParenthesis = explicitParameterTypes || parameterTypes.size() != 1

    val manager = TemplateManager.getInstance(project)

    val template = manager.createTemplate("", "")
    template.setToShortenLongNames(true)
    //template.setToReformat(true) //TODO
    template.addTextSegment("{ ")
    if (useParenthesis) {
        template.addTextSegment("(")
    }

    for ((i, parameterType) in parameterTypes.withIndex()) {
        if (i > 0) {
            template.addTextSegment(", ")
        }
        //TODO: check for names in scope
        template.addVariable(ParameterNameExpression(JetNameSuggester.suggestNames(parameterType, EmptyValidator, "p")), true)
        if (explicitParameterTypes) {
            template.addTextSegment(": " + IdeDescriptorRenderers.SOURCE_CODE.renderType(parameterType))
        }
    }

    if (useParenthesis) {
        template.addTextSegment(")")
    }
    template.addTextSegment(" -> ")
    template.addEndVariable()
    template.addTextSegment(" }")
    return template
}

private class ParameterNameExpression(val nameSuggestions: Array<String>) : Expression() {
    override fun calculateResult(context: ExpressionContext?) = TextResult(nameSuggestions[0])

    override fun calculateQuickResult(context: ExpressionContext?): Result? = null

    override fun calculateLookupItems(context: ExpressionContext?)
            = Array<LookupElement>(nameSuggestions.size(), { LookupElementBuilder.create(nameSuggestions[it]) })
}

fun functionParameterTypes(functionType: JetType): List<JetType>
        = KotlinBuiltIns.getParameterTypeProjectionsFromFunctionType(functionType).map { it.getType() }
