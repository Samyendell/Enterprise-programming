import { useState } from 'react';

export default function SearchBar({ onSearch, onClear }) {
    const [q, setQ] = useState('');
    const handleKey = (e) => { if (e.key === 'Enter') onSearch(q); };
    return (
        <div className="input-group mb-3">
            <input
                type="text"
                className="form-control"
                placeholder="Search by title, author or genre..."
                maxLength={200}
                value={q}
                onChange={(e) => setQ(e.target.value)}
                onKeyUp={handleKey}
            />
            <button className="btn btn-outline-secondary" onClick={() => onSearch(q)}>Go</button>
            <button className="btn btn-outline-secondary" onClick={() => { setQ(''); onClear(); }}>Reset</button>
        </div>
    );
}
