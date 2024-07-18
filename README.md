***App setup on the phone***
- Add 'TaskMan' in Smartwatch app notification list
- Notifications setup (S24 ultra)
    - Long press task notification
    - disable "badge" & "pop-up", but keep enabled "lock screen"
    - Notification categories
        - Forgrond service -> disable
        - 

***Download APK***

- https://github.com/mprashant04/TaskMan/tree/main/app/build/outputs/apk

if direct APK installation fails on the phone, then download apk on the PC and install it using following command

- `adb install -t <filename>.apk`
- if second clone app also gets created, then just uninstall manually this clone app




***How To create initial db manually***

- `cd %LOCALAPPDATA%\Android\sdk\platform-tools`
- `adb shell`
- > `cd /storage/emulated/0/_my/todo`
- > `sqlite3 tasks.db`
    - sqlite> `CREATE TABLE tasks (_id INTEGER PRIMARY KEY, title TEXT, notes TEXT, tag TEXT, due_on INTEGER,  rec_duration INTEGER, rec_first_due_on INTEGER, rec_unit TEXT, type TEXT, status TEXT, xtra_flags TEXT,  createdOn INTEGER,  lastUpdatedOn INTEGER);`
    - sqlite> `CREATE TABLE conf (_id INTEGER PRIMARY KEY, name TEXT, value TEXT, createdOn INTEGER,  lastUpdatedOn INTEGER);`
- then copy tasks.db file to _my/todo/ folder in phone





***Drawable icons***

- https://fonts.google.com/icons
