/**
 * RawViewer — displays the raw API response text alongside the HTTP method.
 *
 * NOTE: this component is key for demonstrating format negotiation.
 * When the user switches format (JSON/XML/Text), the raw viewer shows
 * exactly what the server sent back in that format, proving that the
 * client correctly requests and parses each format.
 *
 * Props:
 *   method - the HTTP method and URL string (e.g. "GET http://...")
 *   text   - the raw response body from the server
 */
export default function RawViewer({ method, text }) {
  return (
    <div className="card border-0 shadow-sm">
      <div className="card-header bg-white py-3">
        <h6 className="mb-0">
          Raw API Response
          {method && <small className="text-muted ms-2 fw-normal">{method}</small>}
        </h6>
      </div>
      <div className="card-body p-2">
        <pre className="raw-viewer mb-0">{text || 'No request sent yet.'}</pre>
      </div>
    </div>
  );
}
