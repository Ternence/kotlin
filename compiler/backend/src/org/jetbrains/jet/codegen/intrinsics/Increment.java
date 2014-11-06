/*
 * Copyright 2010-2013 JetBrains s.r.o.
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

package org.jetbrains.jet.codegen.intrinsics;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.codegen.ExpressionCodegen;
import org.jetbrains.jet.codegen.StackValue;
import org.jetbrains.jet.lang.psi.JetExpression;
import org.jetbrains.jet.lang.psi.JetPsiUtil;
import org.jetbrains.jet.lang.psi.JetReferenceExpression;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.lang.types.lang.KotlinBuiltIns;
import org.jetbrains.org.objectweb.asm.Type;
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter;

import java.util.List;

import static org.jetbrains.jet.codegen.AsmUtil.genIncrement;
import static org.jetbrains.jet.codegen.AsmUtil.isPrimitive;
import static org.jetbrains.jet.lang.resolve.BindingContext.EXPRESSION_TYPE;

public class Increment extends IntrinsicMethod {
    private final int myDelta;

    public Increment(int delta) {
        myDelta = delta;
    }

    @NotNull
    @Override
    public Type generateImpl(
            @NotNull ExpressionCodegen codegen,
            @NotNull InstructionAdapter v,
            @NotNull Type returnType,
            PsiElement element,
            List<JetExpression> arguments,
            StackValue receiver
    ) {
        assert isPrimitive(returnType) : "Return type of Increment intrinsic should be of primitive type : " + returnType;

        if (arguments.size() > 0) {
            JetExpression operand = JetPsiUtil.deparenthesize(arguments.get(0));
            if (operand instanceof JetReferenceExpression && returnType == Type.INT_TYPE) {
                int index = codegen.indexOfLocal((JetReferenceExpression) operand);
                if (index >= 0) {
                    JetType operandType = codegen.getBindingContext().get(EXPRESSION_TYPE, operand);
                    if (operandType != null && KotlinBuiltIns.getInstance().isPrimitiveType(operandType)) {
                        StackValue.preIncrementForLocalVar(index, myDelta).put(returnType, v);
                        return returnType;
                    }
                }
            }
            StackValue value = StackValue.preIncrement(returnType, codegen.genQualified(receiver, operand), myDelta, this, null, codegen);
            value.put(returnType, v);
        }
        else {
            receiver.put(returnType, v);
            genIncrement(returnType, myDelta, v);
        }

        return returnType;
    }
}
