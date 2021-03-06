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

package org.jetbrains.kotlin.codegen.defaultConstructor;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.InnerTestClasses;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.JetTestUtils;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("compiler/testData/codegen/defaultArguments/reflection")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class DefaultArgumentsReflectionTestGenerated extends AbstractDefaultArgumentsReflectionTest {
    public void testAllFilesPresentInReflection() throws Exception {
        JetTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("compiler/testData/codegen/defaultArguments/reflection"), Pattern.compile("^(.+)\\.kt$"), true);
    }

    @TestMetadata("classInClassObject.kt")
    public void testClassInClassObject() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("compiler/testData/codegen/defaultArguments/reflection/classInClassObject.kt");
        doTest(fileName);
    }

    @TestMetadata("classInObject.kt")
    public void testClassInObject() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("compiler/testData/codegen/defaultArguments/reflection/classInObject.kt");
        doTest(fileName);
    }

    @TestMetadata("classWithTwoDefaultArgs.kt")
    public void testClassWithTwoDefaultArgs() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("compiler/testData/codegen/defaultArguments/reflection/classWithTwoDefaultArgs.kt");
        doTest(fileName);
    }

    @TestMetadata("classWithVararg.kt")
    public void testClassWithVararg() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("compiler/testData/codegen/defaultArguments/reflection/classWithVararg.kt");
        doTest(fileName);
    }

    @TestMetadata("enum.kt")
    public void testEnum() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("compiler/testData/codegen/defaultArguments/reflection/enum.kt");
        doTest(fileName);
    }

    @TestMetadata("internalClass.kt")
    public void testInternalClass() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("compiler/testData/codegen/defaultArguments/reflection/internalClass.kt");
        doTest(fileName);
    }

    @TestMetadata("privateClass.kt")
    public void testPrivateClass() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("compiler/testData/codegen/defaultArguments/reflection/privateClass.kt");
        doTest(fileName);
    }

    @TestMetadata("privateConstructor.kt")
    public void testPrivateConstructor() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("compiler/testData/codegen/defaultArguments/reflection/privateConstructor.kt");
        doTest(fileName);
    }

    @TestMetadata("publicClass.kt")
    public void testPublicClass() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("compiler/testData/codegen/defaultArguments/reflection/publicClass.kt");
        doTest(fileName);
    }

    @TestMetadata("publicClassWoDefArgs.kt")
    public void testPublicClassWoDefArgs() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("compiler/testData/codegen/defaultArguments/reflection/publicClassWoDefArgs.kt");
        doTest(fileName);
    }

    @TestMetadata("publicInnerClass.kt")
    public void testPublicInnerClass() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("compiler/testData/codegen/defaultArguments/reflection/publicInnerClass.kt");
        doTest(fileName);
    }

    @TestMetadata("publicInnerClassInPrivateClass.kt")
    public void testPublicInnerClassInPrivateClass() throws Exception {
        String fileName = JetTestUtils.navigationMetadata("compiler/testData/codegen/defaultArguments/reflection/publicInnerClassInPrivateClass.kt");
        doTest(fileName);
    }
}
