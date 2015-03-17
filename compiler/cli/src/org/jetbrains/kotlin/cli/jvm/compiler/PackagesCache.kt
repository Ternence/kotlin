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

package org.jetbrains.kotlin.cli.jvm.compiler

import com.intellij.openapi.vfs.VirtualFile
import java.util.ArrayList
import java.util.HashMap

class PackagesCache(private val classpath: ClassPath) {

    private val _rootCache = RootCache()

    private val maxIndex = classpath.roots.size()

    fun findPackages(path: Array<String>): Iterator<VirtualFile> {
        if (path.isEmpty()) return classpath.iterator()

        recFindPackages(path, pathEntryIndex = 0, rootCache = _rootCache)
    }

    fun recFindPackages(path: Array<String>, pathEntryIndex: Int, rootCache: RootCache) {
        val packageSubName = path[pathEntryIndex]
        val cache = rootCache[packageSubName]
        var index = 0
        for (i in cache.indices) {
            index = i
            if (oob(index)) return

            val file = advance(i, path, pathEntryIndex)

        }
    }

    private fun advance(classpathRootIndex: Int, packagesPath: Array<String>, pathEntryIndex: Int): VirtualFile? {
        var currentFile = classpath.roots[classpathRootIndex]
        for (i in 0..pathEntryIndex) {
            currentFile = currentFile.findChild(packagesPath[i]) ?: return null
        }
        return currentFile
    }

    private fun oob(index: Int): Boolean {
        return index >= maxIndex
    }

    open class RootCache {
        private val innerCaches = HashMap<String, Cache>()

        fun get(name: String) = innerCaches.getOrPut(name) { Cache() }
    }

    class Cache : RootCache() {
        val indices = ArrayList<Int>()
    }
}