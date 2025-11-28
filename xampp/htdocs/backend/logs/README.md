# Logs Directory

API logs are stored here.

- Log files are named `api_YYYY-MM-DD.log`
- Logs include endpoint hits, success/error responses, and auth events
- Logs are also output to `error_log` for real-time terminal viewing

## View Logs in Real-Time

When running `php -S 0.0.0.0:8000`, logs will appear in the terminal.

## Log File Format

```
[2025-11-28 10:30:45] GET    /api/landing | IP: 127.0.0.1 | UA: Mozilla/5.0...
[2025-11-28 10:30:45] 🔓 AUTH OK /api/landing | User: 1
[2025-11-28 10:30:45] ✅ SUCCESS /api/landing | Landing data fetched
```
