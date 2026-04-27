# IRCTC (Train Booking System) - CLI Java App

This project is a simple console-based train booking system built with Java + Gradle. It supports user signup/login, searching trains, booking seats, viewing bookings, and cancelling bookings with local JSON persistence.

## Features Implemented

- **Signup (Option 1)**: Creates a new user (prevents duplicate usernames) and logs the user in.
- **Login (Option 2)**: Validates username/password (bcrypt) and starts a logged-in session.
- **Search Trains (Option 4)**: Searches trains by source -> destination and shows station times.
- **Book Seat (Option 5)**:
  - Requires a previously selected train (from Option 4).
  - Books a seat (updates train seat matrix).
  - Creates a ticket and saves it under the logged-in user.
- **Fetch Bookings (Option 3)**: Prints all tickets for the logged-in user.
- **Cancel Booking (Option 6)**: Cancels a ticket by ticket id and persists the change.

## Local Database (JSON persistence)

The app persists data to a local folder:

- Default location: `./localDb/` (relative to where you run the app)
  - `localDb/users.json`
  - `localDb/trains.json`

On first run, these files are **seeded** from classpath resources in `app/src/main/resources/`.

Optional override:

- Set `IRCTC_DB_DIR` to store the DB in a custom folder.

## How to Run

- Build: `.\gradlew.bat :app:build`
- Run: `.\gradlew.bat :app:run`

## Changes Summary (what we fixed/added)

- **Fixed JSON mapping**: Enabled Jackson `SNAKE_CASE` so JSON keys like `user_id` / `train_id` correctly map to Java fields.
- **Fixed DB path issues**: Removed hardcoded/relative file paths and introduced a single DB resolver + seeding helper.
- **Added persistence for bookings**: Seat booking now also creates/saves a `Ticket` under the user (not just seat matrix updates).
- **Wired cancel flow**: Option 6 now cancels by ticket id and saves to `users.json`.
- **Improved menu robustness**: Better input validation and safer flow when train isn't selected before booking.
- **Fixed build failure**: Updated the generated test that referenced a non-existent `getGreeting()` method.
