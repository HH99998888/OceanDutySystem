CREATE TABLE IF NOT EXISTS monitor_site (id INTEGER PRIMARY KEY AUTOINCREMENT, site_name TEXT NOT NULL, site_url TEXT NOT NULL, site_type TEXT, status TEXT DEFAULT 'UNKNOWN', response_time INTEGER, last_check_time TEXT, error_message TEXT);
CREATE TABLE IF NOT EXISTS monitor_module (id INTEGER PRIMARY KEY AUTOINCREMENT, site_id INTEGER NOT NULL, module_name TEXT NOT NULL, module_url TEXT NOT NULL, update_time TEXT, expected_time TEXT, status TEXT DEFAULT 'UNKNOWN', remark TEXT);
CREATE TABLE IF NOT EXISTS monitor_record (id INTEGER PRIMARY KEY AUTOINCREMENT, site_id INTEGER, module_id INTEGER, check_time TEXT NOT NULL, status TEXT NOT NULL, detail TEXT);
CREATE TABLE IF NOT EXISTS duty_log (id INTEGER PRIMARY KEY AUTOINCREMENT, user_name TEXT NOT NULL, duty_time TEXT NOT NULL, problem TEXT, solution TEXT, recover_time TEXT);
CREATE TABLE IF NOT EXISTS server_check (id INTEGER PRIMARY KEY AUTOINCREMENT, directory TEXT NOT NULL, file_name TEXT, modify_time TEXT, file_status TEXT);
