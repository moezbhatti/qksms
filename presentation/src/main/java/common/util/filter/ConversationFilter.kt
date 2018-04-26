/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package common.util.filter

import model.Conversation
import java.util.regex.PatternSyntaxException
import javax.inject.Inject

class ConversationFilter @Inject constructor(private val recipientFilter: RecipientFilter) : Filter<Conversation>() {

    override fun filter(item: Conversation, query: CharSequence): Boolean {

        val snippetMatches =
                if (query.matches(Regex("/.*/"))) {
                    try {
                        Regex(query.substring(1..(query.lastIndex - 1))).containsMatchIn(item.snippet)
                    } catch (e: PatternSyntaxException) {
                        false
                    }
                } else {
                    item.snippet.contains(query)
                }

        val recipientsMatch = item.recipients.any {
            recipient -> recipientFilter.filter(recipient, query)
        }

        return recipientsMatch || snippetMatches

    }

}