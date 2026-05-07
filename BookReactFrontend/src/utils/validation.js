/**
 * Client-side validation that mirrors the server-side Validation.java.
 * Checks length, format, and content rules so the user gets instant feedback.
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

    if (!title || title.length < 5) errs.push('Title must be at least 5 characters.');
    else if (title.length > 150) errs.push('Title must not exceed 150 characters.');

    if (!author || author.length < 5) errs.push('Author must be at least 5 characters.');
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

    if (!genres || genres.length < 3) errs.push('Genres must be at least 3 characters.');
    else if (genres.length > 50) errs.push('Genres must not exceed 50 characters.');

    if (!characters || characters.length < 5) errs.push('Characters must be at least 5 characters.');
    else if (characters.length > 200) errs.push('Characters must not exceed 200 characters.');

    if (!synopsis || synopsis.length < 5) errs.push('Synopsis must be at least 5 characters.');
    else if (synopsis.length > 1000) errs.push('Synopsis must not exceed 1000 characters.');

    return errs;
}
