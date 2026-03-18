@file:Suppress("NAME_SHADOWING")

package org.hydev.back

import org.kohsuke.github.GitHubBuilder
import java.io.IOException

data class DataEdit(
    val filePath: str,
    val content: str
)

private fun ghRepo() = GitHubBuilder()
    .withOAuthToken(secrets.githubToken)
    .build()
    .getRepository(secrets.githubRepo)

/**
 * Commit directly to the repository
 *
 * @param editor String
 * @param edit One edit
 * @param message Commit message
 * @return Commit URL
 */
fun commitDirectly(editor: str, edit: DataEdit, message: str? = null): str
{
    val editor = editor.replace(" ", "-").lowercase()

    val ghRepo = ghRepo()
    val commit = ghRepo.createContent().path(edit.filePath).content(edit.content)
        .message(message ?: "User $editor pushed an edit").commit()

    return commit.commit!!.htmlUrl.toString()
}

/**
 * Update an existing file directly on GitHub.
 *
 * @throws IOException if the file does not exist or update fails.
 */
fun updateFileDirectly(editor: str, edit: DataEdit, message: str? = null): str
{
    val editor = editor.replace(" ", "-").lowercase()
    val ghRepo = ghRepo()
    val existing = ghRepo.getFileContent(edit.filePath)
    val commit = existing.update(edit.content, message ?: "User $editor updated an edit").commit
    return commit.htmlUrl.toString()
}

/**
 * Find comment file path under people/<personId>/comments with name ending in -C<commentId>.json
 */
fun findApprovedCommentFilePath(personId: str, commentId: int): str?
{
    val ghRepo = ghRepo()
    val dir = "people/$personId/comments"
    val suffix = "-C$commentId.json"

    return try {
        ghRepo.getDirectoryContent(dir)
            .firstOrNull { it.type == "file" && it.name.endsWith(suffix) }
            ?.path
    } catch (_: IOException) {
        null
    }
}
