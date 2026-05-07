import { useState } from 'react';

/**
 * NavBar — top navigation bar with format selector and "New Entry" button.
 *
 * NOTE: the format dropdown demonstrates HTTP content negotiation —
 * when the user changes format, the entire app switches to requesting
 * and sending data in that format (JSON, XML, or Text).
 *
 * Props:
 *   format         - currently selected format ('json', 'xml', 'text')
 *   onFormatChange - callback when user picks a different format
 *   onAddClick     - callback to open the add-book modal
 */
export default function NavBar({ format, onFormatChange, onAddClick }) {
  return (
    <nav className="navbar navbar-custom mb-4">
      <div className="container-fluid">
        <div className="d-flex align-items-center gap-3">
          <span className="navbar-brand text-white fw-bold mb-0">Book Library</span>
          <button className="btn btn-light btn-sm" onClick={onAddClick}>
            New Entry
          </button>
        </div>
        <div className="d-flex align-items-center gap-3">
          <label className="text-white-50 small mb-0">Format:</label>
          <select
            className="form-select form-select-sm"
            style={{ width: 'auto' }}
            value={format}
            onChange={(e) => onFormatChange(e.target.value)}
          >
            <option value="json">JSON</option>
            <option value="xml">XML</option>
            <option value="text">TEXT</option>
          </select>
        </div>
      </div>
    </nav>
  );
}
