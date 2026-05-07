/** Raw API response viewer panel */
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
