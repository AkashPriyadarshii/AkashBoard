/*
 * Copyright (C) 2026 AkashBoard Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package dev.patrickgold.florisboard.dictate.data.prompts

/**
 * Hosting locations for the community prompt library (issue #105).
 *
 * Same zero-ops, self-hosted pattern as the glide dictionaries and Whisper models: a single static
 * JSON file lives on the project's own repository, and the app fetches it on demand (only when the
 * user opens the library browser — never on launch). No third-party service is involved.
 *
 * The library lives on its own orphan branch [BRANCH] rather than in a release asset, so community
 * members can extend it with an ordinary pull request against [LIBRARY_PATH] without the maintainer
 * having to cut a release for every added prompt.
 */
object PromptLibraryCatalog {
    private const val OWNER = "AkashPriyadarshii"
    private const val REPO = "AkashBoard"

    /** Branch that holds the library file. Single re-point for hosting. */
    const val BRANCH = "prompt-library"

    /** Path of the library file within the branch. */
    const val LIBRARY_PATH = "library.json"

    /**
     * Raw URL the app fetches. `raw.githubusercontent.com` serves it from GitHub's CDN with a short
     * (~5 min) cache — perfectly fine for a prompt list that only needs to be eventually consistent.
     */
    const val LIBRARY_URL = "https://raw.githubusercontent.com/$OWNER/$REPO/$BRANCH/$LIBRARY_PATH"

    /**
     * A GitHub "create new file" deep link, pre-filled with [encodedJson], that opens the web editor on
     * a new file under `submissions/`. Non-collaborators are auto-forked by GitHub and land on
     * "Propose new file" → pull request, so a user can contribute a prompt straight from the app with
     * only a GitHub login and no local git. See [PromptLibraryContribution.buildSubmissionUrl].
     */
    fun newFileUrl(fileName: String, encodedJson: String): String =
        "https://github.com/$OWNER/$REPO/new/$BRANCH?filename=submissions/$fileName&value=$encodedJson"

    /** Human-facing landing page for the library (browsed in a normal browser). */
    const val BROWSE_URL = "https://github.com/$OWNER/$REPO/tree/$BRANCH"
}
