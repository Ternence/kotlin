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

package org.jetbrains.kotlin.cli.jvm.compiler;

import com.intellij.core.CoreJavaFileManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiPackageImpl;
import com.intellij.psi.impl.file.impl.JavaFileManager;
import com.intellij.psi.search.GlobalSearchScope;
import jet.runtime.typeinfo.JetValueParameter;
import kotlin.Function1;
import kotlin.KotlinPackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.Name;

import java.util.*;
import java.util.regex.Pattern;

public class CoreJavaFileManagerExt extends CoreJavaFileManager implements JavaFileManager {
    private static final Logger LOG = Logger.getInstance("#com.intellij.core.CoreJavaFileManager");

    private final List<VirtualFile> myClasspath = new ArrayList<VirtualFile>();

    private PackagesCache packagesCache;
    private final PsiManager myPsiManager;

    public CoreJavaFileManagerExt(PackagesCache packagesCache, PsiManager psiManager) {
        super(psiManager);
        this.packagesCache = packagesCache;
        myPsiManager = psiManager;
    }

    public void setPackagesCache(PackagesCache packagesCache) {
        this.packagesCache = packagesCache;
    }

    @Nullable
    public PsiClass findClass(@NotNull ClassId classId, @NotNull final GlobalSearchScope scope) {
        List<Name> packageNameSegments = classId.getPackageFqName().pathSegments();

        final String classNameWithInnerClasses = classId.getRelativeClassName().asString();
        List<String> NAME_IT = KotlinPackage.map(packageNameSegments, new Function1<Name, String>() {
            @Override
            public String invoke(Name name) {
                return name.getIdentifier();
            }
        });
        return packagesCache.searchPackages(NAME_IT, new Function1<VirtualFile, PsiClass>() {
            @Override
            public PsiClass invoke(VirtualFile dir) {
                return findClassGivenPackage(scope, dir, classNameWithInnerClasses);
            }
        });
    }

    @Nullable
    private PsiClass findClassGivenPackage(
            @NotNull GlobalSearchScope scope,
            @NotNull VirtualFile packageDir,
            @NotNull String classNameWithInnerClasses
    ) {
        String topLevelClassName = substringBeforeFirstDot(classNameWithInnerClasses);

        VirtualFile vFile = packageDir.findChild(topLevelClassName + ".class");
        if (vFile == null) vFile = packageDir.findChild(topLevelClassName + ".java");

        if (vFile == null) {
            return null;
        }
        if (!vFile.isValid()) {
            LOG.error("Invalid child of valid parent: " + vFile.getPath() + "; " + packageDir.isValid() + " path=" + packageDir.getPath());
            return null;
        }
        if (!scope.contains(vFile)) {
            return null;
        }

        PsiFile file = myPsiManager.findFile(vFile);
        if (!(file instanceof PsiClassOwner)) {
            return null;
        }

        return findClassInPsiFile(classNameWithInnerClasses, (PsiClassOwner) file);
    }

    @NotNull
    private static String substringBeforeFirstDot(@NotNull String classNameWithInnerClasses) {
        int dot = classNameWithInnerClasses.indexOf('.');
        if (dot < 0) {
            return classNameWithInnerClasses;
        }
        else {
            return classNameWithInnerClasses.substring(0, dot);
        }
    }

    @Nullable
    private static PsiClass findClassInPsiFile(@NotNull String classNameWithInnerClassesDotSeparated, @NotNull PsiClassOwner file) {
        for (PsiClass topLevelClass : file.getClasses()) {
            PsiClass candidate = findClassByTopLevelClass(classNameWithInnerClassesDotSeparated, topLevelClass);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    @Nullable
    private static PsiClass findClassByTopLevelClass(@NotNull String className, @NotNull PsiClass topLevelClass) {
        if (className.indexOf('.') < 0) {
            return className.equals(topLevelClass.getName()) ? topLevelClass : null;
        }

        Iterator<String> segments = StringUtil.split(className, ".").iterator();
        if (!segments.hasNext() || !segments.next().equals(topLevelClass.getName())) {
            return null;
        }
        PsiClass curClass = topLevelClass;
        while (segments.hasNext()) {
            String innerClassName = segments.next();
            PsiClass innerClass = curClass.findInnerClassByName(innerClassName, false);
            if (innerClass == null) {
                return null;
            }
            curClass = innerClass;
        }
        return curClass;
    }


    @Override
    public void addToClasspath(VirtualFile root) {
        super.addToClasspath(root);
        myClasspath.add(root);
    }
}
