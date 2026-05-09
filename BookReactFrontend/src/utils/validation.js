/**
 * This file provides the client side validation, it follows the same patterns used 
 * on the server side.
 */

const AUTHOR_REGEX = /^[a-zA-Z .'\-]+$/;
const DATE_REGEX = /^[0-9/\-]+$/;

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
    else if (!DATE_REGEX.test(date.trim())) errs.push('Date may only contain numbers, hyphens, and forward slashes.');

    if (genres && genres.length > 200) errs.push('Genres must not exceed 200 characters.');

    if (synopsis && synopsis.length > 8000) errs.push('Synopsis must not exceed 8000 characters.');

    return errs;
}
