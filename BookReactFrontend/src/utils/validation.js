/**
 * Client-side validation that mirrors the server-side Validation.java.
 *
 * NOTE: by duplicating the validation rules on the client, the user gets
 * instant feedback without a round-trip to the server. The same rules
 * are enforced server-side (in Validation.java) as a safety net, so
 * even if client-side validation is bypassed, bad data cannot be saved.
 *
 * Rules checked:
 *   - Title: required, max 150 characters
 *   - Author: required, max 150 characters, letters/spaces/hyphens/apostrophes only
 *   - Date: required, 4-digit year, not in the future, >= 1000
 *   - Genres: optional, max 200 characters
 *   - Characters: optional, no limit
 *   - Synopsis: optional, max 8000 characters
 *   - All fields: HTML tags are stripped before checking (XSS prevention)
 */

const CURRENT_YEAR = new Date().getFullYear();
const AUTHOR_REGEX = /^[a-zA-Z .'\-]+$/;
const YEAR_REGEX = /^\d{4}$/;

function stripHtml(s) {
    return s ? s.replace(/<[^>]*>/g, '').trim() : '';
}

export function validateBook(b) {
    const errs = [];
    const title = stripHtml(b.title);
    const author = stripHtml(b.author);
    const date = stripHtml(b.date);
    const genres = stripHtml(b.genres);
    const characters = stripHtml(b.characters);
    const synopsis = stripHtml(b.synopsis);

    if (!title) errs.push('Title is required.');
    else if (title.length > 150) errs.push('Title must not exceed 150 characters.');

    if (!author) errs.push('Author is required.');
    else if (author.length > 150) errs.push('Author must not exceed 150 characters.');
    else if (!AUTHOR_REGEX.test(author))
        errs.push('Author must only contain letters, spaces, hyphens, and apostrophes.');

    if (!date || !date.trim()) errs.push('Date is required.');
    else if (!YEAR_REGEX.test(date.trim())) errs.push('Date must be a valid 4-digit year (e.g. 2003).');
    else {
        const y = parseInt(date.trim());
        if (y > CURRENT_YEAR) errs.push('Date cannot be in the future.');
        else if (y < 1000) errs.push('Date must be a realistic year (1000 or later).');
    }

    if (genres && genres.length > 200) errs.push('Genres must not exceed 200 characters.');

    // Characters: optional, no length limit

    if (synopsis && synopsis.length > 8000) errs.push('Synopsis must not exceed 8000 characters.');

    return errs;
}
