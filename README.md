# TimeChanger LSPosed Module for Android 10

This module allows you to change the system time to a fixed date (2025-05-16) and prevents the system from automatically syncing back to the correct time.

## Features

- Forces system time to 2025-05-16
- Blocks automatic time sync from the system
- Automatically restores original time when the frontend app exits
- Simple UI to toggle time modification

## Requirements

- Android 10 (API level 29)
- LSPosed framework installed
- Root access

## Installation Instructions

1. Install the LSPosed framework on your device if you haven't already
2. Build and install this module
3. Activate the module in LSPosed Manager
4. Enable system scope for the module
5. Reboot your device
6. Open the TimeChanger app to toggle time modification

## Usage

1. Open the TimeChanger app
2. Press the "Enable Time Modification" button
3. System time will be changed to 2025-05-16
4. Launch your frontend app
5. When your frontend app exits, the time will automatically reset to the original time

## Customization

To change the target time, modify the constants in `TimeChangerHook.java`:

```java
private static final int TARGET_YEAR = 2025;
private static final int TARGET_MONTH = 4; // May (0-based index)
private static final int TARGET_DAY = 16;
```

To specify your frontend app package name, modify the constant in `TimeChangerHook.java`:

```java
private static final String FRONTEND_PACKAGE = "com.example.yourapp";
```

## How It Works

The module hooks into various system APIs related to time:

1. System.currentTimeMillis()
2. SystemClock.currentTimeMillis()
3. Calendar.getInstance()
4. Date constructors
5. Settings to disable automatic time sync

When enabled, these hooks return the modified time. The module also monitors for your frontend app's exit to restore the original time. 