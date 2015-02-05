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

package org.jetbrains.kotlin.idea.j2k

import org.jetbrains.kotlin.j2k.JavaToKotlinConversionService
import org.jetbrains.kotlin.j2k.ConversionScope
import org.jetbrains.kotlin.j2k.JavaToKotlinConverter
import org.jetbrains.kotlin.j2k.ConverterSettings
import org.jetbrains.kotlin.j2k.FilesConversionScope
import org.jetbrains.kotlin.j2k.IdeaReferenceSearcher
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.JetElement
import com.intellij.psi.PsiJavaFile
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.psi.JetPsiFactory
import com.intellij.psi.PsiMember
import org.jetbrains.kotlin.psi.JetNamedDeclaration
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.JetPsiUtil

public class IdeaJavaToKotlinConverterFactory(private val project: Project): JavaToKotlinConversionService {
    private val psiFactory = JetPsiFactory(project)

    override fun canConvert(elementToConvert: PsiMember): Boolean {
        return elementToConvert.getLanguage() == JavaLanguage.INSTANCE && elementToConvert.getContainingFile() is PsiJavaFile
    }

    override fun convert(elementToConvert: PsiMember, contextToAnalyzeIn: PsiElement): JetNamedDeclaration {
        assert(canConvert(elementToConvert), "canConvert() must return true: ${JetPsiUtil.getElementTextWithContext(elementToConvert)}")
        val converter = JavaToKotlinConverter(
                project,
                ConverterSettings.defaultSettings,
                FilesConversionScope(listOf(elementToConvert.getContainingFile() as PsiJavaFile)),
                IdeaReferenceSearcher,
                IdeaResolverForConverter
        )
        val text = converter.elementsToKotlin(listOf(elementToConvert to J2kPostProcessor(contextToAnalyzeIn))).first()
        return psiFactory.createDeclaration(text)
    }
}
