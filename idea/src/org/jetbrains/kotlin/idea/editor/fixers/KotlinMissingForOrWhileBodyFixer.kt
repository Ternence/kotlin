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

package org.jetbrains.kotlin.idea.editor.fixers

import com.intellij.lang.SmartEnterProcessorWithFixers
import org.jetbrains.kotlin.idea.editor.KotlinSmartEnterHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.JetWhileExpression
import org.jetbrains.kotlin.psi.JetBlockExpression
import org.jetbrains.kotlin.psi.JetLoopExpression
import org.jetbrains.kotlin.psi.JetForExpression

public class KotlinMissingForOrWhileBodyFixer : SmartEnterProcessorWithFixers.Fixer<KotlinSmartEnterHandler>() {
    override fun apply(editor: Editor, processor: KotlinSmartEnterHandler, element: PsiElement) {
        if (!(element is JetForExpression || element is JetWhileExpression)) return
        val loopExpression = element as JetLoopExpression

        val doc = editor.getDocument()

        val body = loopExpression.getBody()
        if (body is JetBlockExpression) return

        if (!loopExpression.isValidLoopCondition()) return

        if (body != null && body.startLine(doc) == loopExpression.startLine(doc)) return

        val rParen = loopExpression.getRightParenthesis()
        if (rParen == null) return

        doc.insertString(rParen.range.end, "{}")
    }

    fun JetLoopExpression.isValidLoopCondition() = getLeftParenthesis() != null && getRightParenthesis() != null
}

