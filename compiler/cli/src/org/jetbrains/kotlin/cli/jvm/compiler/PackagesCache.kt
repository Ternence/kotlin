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
import kotlin.properties.Delegates

class PackagesCache(private val classpath: List<VirtualFile>) {

    //these fields are computed based on classpath which is filled in later
    private val maxIndex: Int by Delegates.lazy { classpath.size() }

    private val rootCache: Cache by Delegates.lazy {
        with(Cache()) {
            classpathIndices.addAll(classpath.indices.toList())
            classpathIndices.add(maxIndex)
            this
        }
    }

    inline fun <T : Any> searchPackages(packagesPath: Array<String>, handler: (VirtualFile) -> T?): T? {
        if (packagesPath.isEmpty()) {
            classpath.forEach { file ->
                val result = handler(file)
                if (result != null) return result
            }
        }

        val caches = cachesPath(packagesPath)

        var lastMaxIndex = -1
        for (cacheIndex in (caches.size() - 1)..0) {
            val cache = caches[cacheIndex]
            for (classpathIndex in cache.classpathIndices) {
                if (classpathIndex <= lastMaxIndex) continue

                val file = travelPath(classpathIndex, packagesPath, cacheIndex, caches) ?: continue
                val result = handler(file)
                if (result != null) {
                    return result
                }
            }
            lastMaxIndex = cache.classpathIndices.last()
        }
        return null
    }

    /**
     * root -> "org" -> "jet" -> "language"
     * [org, jet, language]
     */
    private fun travelPath(classPathEntryIndex: Int, packagesPath: Array<String>, fillCachesAfter: Int, cachesPath: List<Cache>): VirtualFile? {
        if (oob(classPathEntryIndex)) {
            for (i in (fillCachesAfter + 1)..cachesPath.size() - 1) {
                cachesPath[i].classpathIndices.add(maxIndex)
            }
        }

        var currentFile = classpath[classPathEntryIndex]
        for (pathIndex in 0..packagesPath.size()) {
            currentFile = currentFile.findChild(packagesPath[pathIndex]) ?: return null
            val correspondingCacheIndex = pathIndex + 1
            if (correspondingCacheIndex > fillCachesAfter) {
                cachesPath[correspondingCacheIndex].classpathIndices.add(classPathEntryIndex)
            }
        }
        return currentFile
    }

    private fun cachesPath(path: Array<String>): ArrayList<Cache> {
        val caches = ArrayList<Cache>()
        caches.add(rootCache)
        var currentCache = rootCache
        for (subPackageName in path) {
            currentCache = currentCache[subPackageName]
            caches.add(currentCache)
        }
        return caches
    }

    private fun oob(index: Int): Boolean {
        return index >= maxIndex
    }

    class Cache {
        private val innerCaches = HashMap<String, Cache>()

        fun get(name: String) = innerCaches.getOrPut(name) { Cache() }

        val classpathIndices = ArrayList<Int>()
    }
}