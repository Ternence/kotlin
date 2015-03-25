/*
 * Copyright 2010-2014 JetBrains s.r.o.
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

package kotlin

/**
 * Returns `true` if this character (Unicode code point) is defined in Unicode.
 */
public fun Char.isDefined(): Boolean = Character.isDefined(this)

/**
 * Returns `true` if this character (Unicode code point) is a digit.
 */
public fun Char.isDigit(): Boolean = Character.isDigit(this)

/**
 * Returns `true` if this character is a Unicode high-surrogate code unit (also known as leading-surrogate code unit).
 */
public fun Char.isHighSurrogate(): Boolean = Character.isHighSurrogate(this)

/**
 * Returns `true` if this character (Unicode code point) should be regarded as an ignorable
 * character in a Java identifier or a Unicode identifier.
 */
public fun Char.isIdentifierIgnorable(): Boolean = Character.isIdentifierIgnorable(this)

/**
 * Returns `true` if this character is an ISO control character.
 */
public fun Char.isISOControl(): Boolean = Character.isISOControl(this)

/**
 * Returns `true` if this  character (Unicode code point) may be part of a Java identifier as other than the first character.
 */
public fun Char.isJavaIdentifierPart(): Boolean = Character.isJavaIdentifierPart(this)

/**
 * Returns `true` if this character is permissible as the first character in a Java identifier.
 */
public fun Char.isJavaIdentifierStart(): Boolean = Character.isJavaIdentifierStart(this)

deprecated("Please use Char.isJavaIdentifierStart() instead")
public fun Char.isJavaLetter(): Boolean = Character.isJavaLetter(this)

deprecated("Please use Char.isJavaIdentifierPart() instead")
public fun Char.isJavaLetterOrDigit(): Boolean = Character.isJavaLetterOrDigit(this)

/**
 * Determines whether a character is whitespace according to the Unicode standard.
 * Returns `true` if the character is whitespace.
 */
public fun Char.isWhitespace(): Boolean = Character.isWhitespace(this) || Character.isSpaceChar(this)

/**
 * Returns `true` if this character is upper case.
 */
public fun Char.isUpperCase(): Boolean = Character.isUpperCase(this)

/**
 * Returns `true` if this character is lower case.
 */
public fun Char.isLowerCase(): Boolean = Character.isLowerCase(this)

/**
 * Converts this character to uppercase.
 */
public fun Char.toUpperCase(): Char = Character.toUpperCase(this)

/**
 * Converts this character to lowercase.
 */
public fun Char.toLowerCase(): Char = Character.toLowerCase(this)

/**
 * Returns `true` if this character is equal to the [other] character, optionally ignoring character case.
 *
 * @param ignoreCase `true` to ignore character case when comparing characters. By default `false`.
 *
 * Two characters are considered the same ignoring case if at least one of the following is true:
 *   - The two characters are the same (as compared by the == operator)
 *   - Applying the method [toUpperCase] to each character produces the same result
 *   - Applying the method [toLowerCase] to each character produces the same result
 */
public fun Char.equals(other: Char, ignoreCase: Boolean = false): Boolean {
    if (this === other) return true
    if (!ignoreCase) return false

    if (this.toUpperCase() === other.toUpperCase()) return true
    if (this.toLowerCase() === other.toLowerCase()) return true
    return false
}
