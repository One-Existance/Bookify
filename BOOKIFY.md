# Bookify — Android App Context

## What This App Is
Bookify is an Android event booking app for Tanzania.
- **Group:** DIT Group No. 8, class OD23IT
- **Team:** Sunday Julius Kihiyo, Sarah John Mahwera, Omary Isihaka Athuman, Yusuph Samwel Mniko, Peter Ladislaus Msimbira

---

## Language & Tech Stack
- **Language: Java only — NOT Kotlin.** Never add `.kt` files.
- **UI: XML Views** — no Jetpack Compose
- **Database: SQLite** via `DatabaseHelper.java` (schema version 8) — local profile data only; credentials now live in Firebase Auth (see below)
- **Auth: Firebase Authentication** (email/password) via `google-services` plugin + `firebase-auth`/`firebase-analytics` (BOM `34.16.0`). `LoginActivity`/`RegisterActivity` call `FirebaseAuth` directly; the local `users` table stores only the profile (name, phone, `role`), linked by a `firebase_uid` column. Requires `app/google-services.json` (gitignored, not committed — see Important Notes)
- **Roles: three-tier (`USER` / `PROMOTER` / `ADMIN`)** stored in `users.role` — see "Roles & Event-Organizing Flow" below
- **Session: SharedPreferences** — key `"bookify_session"`, stores `user_id` (int), `user_name`, `user_email`, `role` (String: `USER`/`PROMOTER`/`ADMIN`). Saved on login/register, cleared on logout (also signs out of Firebase Auth).
- **UI Libraries:** AppCompat, Material Components, RecyclerView, CardView, ConstraintLayout
- **QR Codes:** ZXing (`com.google.zxing:core:3.5.3`) generates QR bitmaps from ticket numbers (per-attendee, `TicketDetailActivity`) and from event `access_code`s (per-event entry QR, `util/InviteShareHelper`, any PUBLISHED event public or private). **Scanning:** `com.journeyapps:zxing-android-embedded:4.3.0` + `ScanEntryActivity` — camera-based scan screen (needs `CAMERA` permission, requested at runtime) reachable from a "Scan Entry" button on organizer/admin event lists. Scans either a ticket QR (`DatabaseHelper.checkInTicket()` — real per-attendee check-in with anti-duplicate/wrong-event/unpaid rejection, `bookings.checked_in` column) or an event invite QR (just confirms the code matches this event; that code is shared across all invitees so there's no anti-duplicate protection on that path).
- **Sharing:** `FileProvider` (`res/xml/file_paths.xml`, authority `${applicationId}.fileprovider`) shares generated QR PNGs as `content://` URIs. `util/InviteShareHelper` shares an event's invite text + QR image via the generic Android share sheet or directly through WhatsApp (`setPackage("com.whatsapp")`, falls back to `com.whatsapp.w4b`). Requires `com.whatsapp`/`com.whatsapp.w4b` in the manifest `<queries>` block (Android 11+ package-visibility rule).
- **Payments:** Mongike mobile money gateway (Tanzania: M-Pesa, Tigo, Airtel, Halopesa) — called directly from `PaymentActivity` via `HttpURLConnection`
- **Maps:** Google Maps (`com.google.android.gms:play-services-maps:19.0.0`) via `MapActivity.java` — geocodes an event's location string to a `LatLng` and drops a marker; needs a `MAPS_API_KEY` entry in `local.properties` (currently unset, so the map won't render real tiles until one is added — see Important Notes)
- **Notifications:** Real Android notifications via `util/NotificationHelper.java` — creates the `bookify_notifications` channel, requests `POST_NOTIFICATIONS` at runtime (required on API 33+), and posts notifications gated by both the OS permission and the Settings toggle (`bookify_settings` → `notifications`). Fired on booking confirmation (`EventDetailActivity`), promoter accept/reject (`PromoterDashboardActivity`), and admin promoter-application approve/reject (`AdminActivity`)
- **Permissions:** `INTERNET`, `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` (all in `AndroidManifest.xml`)
- **Dark/light mode:** Runtime-togglable via `AppCompatDelegate.setDefaultNightMode()`, toggle lives in `SettingsActivity` (persisted to `bookify_settings` → `dark_mode`, default dark). Every screen uses semantic theme attrs (`res/values/attrs.xml`: `colorAppBackground`/`colorAppSurface`/`colorAppTextPrimary`/`colorAppTextMuted`/`colorAppChipBg`) resolved by `res/values/themes.xml` (light) vs `res/values-night/themes.xml` (dark); brand accents (purple/amber/card colors) stay constant across both. `BookifyApplication` applies the persisted preference on process start.
- **Language:** English/Swahili via the AndroidX per-app language API (`AppCompatDelegate.setApplicationLocales`, `res/xml/locales_config.xml` + manifest `android:localeConfig`), toggle in `SettingsActivity`. ~237 strings live in `res/values/strings.xml` + `strings_batch_a..d.xml`, translated in matching `res/values-sw/` files. Dynamic/DB-driven text (event data, ticket numbers, exception messages) is intentionally not localized.
- **Event-form fields:** `util/FieldFormatters.java` — `attachDatePicker`/`attachTimePicker` wire a `MaterialDatePicker`/`MaterialTimePicker` onto a (read-only) `EditText`, formatting consistently ("Jul 25, 2025" / "7:00 PM"); `formatPrice(raw)` turns plain digits into "Tsh 15,000". Used by `OrganizeEventActivity`, `AdminActivity`, and `PromoterDashboardActivity`'s Accept dialog — anywhere an event's date/time/price is set or edited.
- **Icons:** ~23 hand-authored vector drawables in `res/drawable/ic_*.xml` (neutral fill, tinted per usage via `app:tint`/`android:tint`) replace emoji used as UI chrome (nav bar, row icons, category icons, info-grid labels, badges) — see `ic_qr_scanner`/`ic_check_circle`/etc. Content/copy emoji (invite message body, greeting flourish) were intentionally left alone; see CHANGES.md Session 3 for the full list of what was/wasn't converted.

---

## All Source Files

### Activities
| File | Purpose |
|------|---------|
| `MainActivity.java` | Splash screen — logo, tagline, "Get started" → Register, "Log in" → Login. **No longer the launcher** — only reached via explicit navigation (e.g. logout); the app now opens straight to `HomeFeedActivity` |
| `LoginActivity.java` | Email + password login via Firebase Auth (`signInWithEmailAndPassword`); links/creates the local profile row by `firebase_uid`; saves session (incl. `role`); routes `ADMIN` → AdminActivity, `PROMOTER` → PromoterDashboardActivity, `USER` → HomeFeedActivity; password field has a working show/hide eye toggle |
| `RegisterActivity.java` | Full name, email, password (min 6 chars, confirm match), phone; creates the account via Firebase Auth (`createUserWithEmailAndPassword`), then a local profile row (always `role=USER`); saves session; both password + confirm-password fields have independent working show/hide eye toggles |
| `HomeFeedActivity.java` | **Launcher activity.** Home feed — dynamic greeting (falls back to "Guest" with no session, which is what makes guest browsing work), avatar (tappable → Profile), category chips, events RecyclerView, bottom nav; single role-aware FAB (`fab_action`) → AdminActivity/PromoterDashboardActivity/OrganizeEventActivity depending on session `role`; requests the `POST_NOTIFICATIONS` runtime permission on first load. Event taps and the FAB are gated by `util/AuthGate` — guests get a Log In/Create Account prompt instead |
| `MapActivity.java` | Shows an event's location on a Google Map — geocodes the location string to a `LatLng`, drops a marker, centers the camera; needs `MAPS_API_KEY` set in `local.properties` to render actual map tiles |
| `ExploreActivity.java` | Browse + live search all events (filters as user types); bottom nav Explore active; event taps gated by `util/AuthGate` same as Home |
| `EventDetailActivity.java` | Event detail — hero card (with image if set), 4-cell info grid, About text; Book Ticket → creates a `PENDING` booking → launches `PaymentActivity` → on success routes to `TicketDetailActivity` to show the QR code |
| `MyTicketsActivity.java` | My tickets — now DB-backed (`db.getUserBookings`) via `BookingAdapter`; only shows `COMPLETED` (paid) bookings; empty state if none; "🔒 Private" link; bottom nav Tickets active |
| `PrivateEventActivity.java` | Private event — access code entry, real DB lookup (`db.getEventByAccessCode`); on match launches `EventDetailActivity` with that event's extras, reusing the normal booking/payment pipeline; bottom nav Tickets active |
| `MyBookingsActivity.java` | My bookings — DB-backed list of current user's bookings via BookingAdapter; shows empty state if none |
| `ProfileActivity.java` | Profile — loads name/email/initials from SharedPreferences (refreshed in `onResume()`); pencil-icon button next to the name opens `EditProfileActivity`; My Bookings, **My Organized Events**, role-aware **Promoter row** (Become a Promoter / pending / Promoter Dashboard, hidden for Admin), Settings rows; logout signs out of Firebase Auth and clears session |
| `EditProfileActivity.java` | Edit full name — pre-fills from session, Save writes to `DatabaseHelper.updateUserName()` and the `bookify_session` prefs, then finishes back to Profile |
| `SettingsActivity.java` | Settings — push notification + email reminder toggles (persisted in `"bookify_settings"` prefs); turning the notification toggle on also requests `POST_NOTIFICATIONS` if not already granted; **Appearance section: Dark mode switch** (`AppCompatDelegate.setDefaultNightMode`) and **Language row** (English/Kiswahili picker dialog, `AppCompatDelegate.setApplicationLocales`); About section |
| `AdminActivity.java` | Admin dashboard (`role=ADMIN` only, guarded in `onCreate()`) — form to post new events (status defaults `PUBLISHED`, no approval loop); RecyclerView of **all** events regardless of status/visibility (`getAllEventsForAdmin`) with delete button; **new: Pending Promoter Applications list** with Approve/Reject (`PromoterApplicationAdapter`); "Logout" and "View as User" links |
| `PaymentActivity.java` | Mongike mobile-money checkout — collects phone number, POSTs to the Mongike API, mock-verifies the payment, then calls `db.completePayment()` to flip the booking to `COMPLETED` and returns `RESULT_OK` |
| `TicketDetailActivity.java` | Shows a booked ticket — title, info, ticket number, event image (if any), and a ZXing-generated QR code encoding the ticket number |
| `BecomePromoterActivity.java` | Form (hall/venue name, location, description) → `db.submitPromoterApplication()`, status `PENDING` until Admin reviews it |
| `PromoterDashboardActivity.java` | `role=PROMOTER` only (guarded) — shows the promoter's approved hall info, and pending event requests (`getPendingEventRequestsForPromoter`) with Accept (dialog to adjust price/date/time before confirming) / Reject actions; "Logout" and "View as User" links |
| `OrganizeEventActivity.java` | Any logged-in user — Spinner of approved promoters (`getApprovedPromoters`) + the event form (same field set/image-picker pattern as AdminActivity, date/time via `util/FieldFormatters` pickers) + a public/private switch; submits via `db.requestEvent()` with `status=PENDING`, not yet visible anywhere |
| `MyEventRequestsActivity.java` | Lists events the current user organized (`getEventsByOrganizer`, any status) with a status badge; for any `PUBLISHED` event (public or private) with an `access_code`, three buttons — "Share Entry QR" (generic share sheet), "WhatsApp" (opens WhatsApp directly) via `util/InviteShareHelper`, and "Scan Entry" (opens `ScanEntryActivity` for that event) |
| `ScanEntryActivity.java` | Camera QR scanner (zxing-android-embedded) for door entry — scans a ticket QR (`db.checkInTicket()`, per-attendee check-in + anti-duplicate) or an event invite QR (validity check only); loops via "Scan Next" |

### Data Layer
| File | Purpose |
|------|---------|
| `data/DatabaseHelper.java` | SQLite (schema v8) — users (local profile only, linked by `firebase_uid`), events, bookings, **promoter_applications** tables; seeds 4 events + 1 admin profile row + 1 test user row; CRUD/search/payment/promoter/organize-request methods |
| `data/User.java` | POJO: id, fullName, email, phone, `role` (String: `USER`/`PROMOTER`/`ADMIN` — constants `ROLE_USER`/`ROLE_PROMOTER`/`ROLE_ADMIN`); helper methods `isAdmin()`/`isPromoter()`/`isUser()` |
| `data/Event.java` | POJO: id, title, location, date, category, price, isPrivate, `imageUrl`, `time`, `slots`, `description`, **`organizerId`, `promoterId`, `status`** (`PENDING`/`PUBLISHED`/`REJECTED`), **`accessCode`** |
| `data/Booking.java` | POJO: ticketNumber, eventTitle, eventDate, eventCategory, eventPrice, `imageUrl`, `status` (`PENDING`/`COMPLETED`) |
| `data/PromoterProfile.java` | POJO: userId, fullName, email, hallName, location, description — an approved promoter's venue info, used in the "choose a promoter" picker |
| `data/PromoterApplication.java` | POJO: id, userId, applicantName, applicantEmail, hallName, location, description, status (`PENDING`/`APPROVED`/`REJECTED`) |
| `data/EventRequest.java` | Small wrapper: `Event` + `organizerName`, used to render the promoter's pending-requests list without a second query per row |

### Adapters
| File | Purpose |
|------|---------|
| `adapter/EventAdapter.java` | Event cards RecyclerView — supports `filter(category)`, `search(query)`, `setOnEventClickListener`; shows event image if `imageUrl` set, else emoji icon |
| `adapter/BookingAdapter.java` | Booking cards RecyclerView — used in MyTicketsActivity/MyBookingsActivity; shows event image if set; tapping a card opens `TicketDetailActivity` (QR code) |
| `adapter/AdminEventAdapter.java` | Admin event-list RecyclerView — shows title/location/date/image/**status badge** per event with a delete button (`OnDeleteClickListener`) |
| `adapter/EventRequestAdapter.java` | Promoter dashboard's pending-request cards — organizer name, event info, Accept/Reject buttons (`OnRequestActionListener`) |
| `adapter/MyEventRequestAdapter.java` | Organizer's "My Organized Events" cards — status badge, conditional "Share Invite Code" button for published private events (`OnShareClickListener`) |
| `adapter/PromoterApplicationAdapter.java` | Admin's pending-promoter-application cards — applicant/hall info, Approve/Reject buttons (`OnApplicationActionListener`) |

### Utilities
| File | Purpose |
|------|---------|
| `util/NotificationHelper.java` | Creates the `bookify_notifications` channel, checks/requests `POST_NOTIFICATIONS` (`hasPermission`/`requestPermissionIfNeeded`), and posts notifications (`notify(...)`) gated by both the OS permission and the Settings toggle |

---

## All XML Layouts
| File | Screen |
|------|--------|
| `activity_main.xml` | Splash — logo, title, tagline, buttons, dot indicators |
| `activity_login.xml` | Login — email/password fields, error label, sign-up link |
| `activity_register.xml` | Register — full name, email, password, phone |
| `activity_home_feed.xml` | Home feed — header + avatar, search bar (display), category chips, RecyclerView, bottom nav |
| `activity_explore.xml` | Explore — functional search EditText, all events RecyclerView, bottom nav |
| `activity_event_detail.xml` | Event detail — hero card (image or emoji), 2×2 info grid, About section, Book Ticket button |
| `activity_my_tickets.xml` | My tickets — Upcoming/Past tabs, RecyclerView (DB-backed) + empty-state view, bottom nav |
| `activity_private_event.xml` | Private event — circle icon, access code input (real), error label, bottom nav |
| `activity_my_bookings.xml` | My bookings — back button, empty state TextView, RecyclerView |
| `activity_profile.xml` | Profile — avatar, name/email + edit-name pencil button, My Bookings row, **My Organized Events row**, **Promoter row**, Settings row, logout button, bottom nav |
| `activity_edit_profile.xml` | Edit Profile — back button, full-name field, Save button |
| `activity_settings.xml` | Settings — back button, notification toggles (SwitchCompat), **Appearance section (Dark mode switch + Language row)**, About rows |
| `activity_admin.xml` | Admin dashboard — event form fields, image picker preview, Save button, **Pending Promoter Applications RecyclerView**, RecyclerView of all events (any status), logout/view-as-user links |
| `activity_payment.xml` | Payment — amount display, phone number input, "Pay Now" button, progress bar |
| `activity_ticket_detail.xml` | Ticket detail — title, info, ticket number, event image, QR code image, back button |
| `activity_become_promoter.xml` | Hall/venue name, location, description fields, Submit button |
| `activity_promoter_dashboard.xml` | Hall info header, "View as Regular User" link, logout, pending-requests RecyclerView + empty state |
| `activity_organize_event.xml` | Promoter Spinner, public/private SwitchCompat, image picker, event form fields, submit button |
| `activity_my_event_requests.xml` | Back button, empty state, RecyclerView of the organizer's requested events |
| `item_event_card.xml` | Event card — image (or emoji fallback), title, location/date, category chip, price chip |
| `item_booking_card.xml` | Booking card — event image, category, title, date+price, dashed divider, ticket number + QR placeholder |
| `item_admin_event.xml` | Admin event row — thumbnail, title, location/date info, **status/visibility badge**, delete button |
| `item_event_request.xml` | Promoter dashboard request card — title, organizer, event info, Reject/Accept buttons |
| `item_my_event_request.xml` | Organizer's event card — title, status badge, info, conditional Share Invite button |
| `item_promoter_application.xml` | Admin's application card — hall name, applicant, location, Reject/Approve buttons |

---

## Drawables
| File | Used for |
|------|----------|
| `bg_button_primary.xml` | Purple rounded button |
| `bg_button_amber.xml` | Amber rounded button (Book Ticket, Access Event) |
| `bg_card_purple.xml` | Concert event/ticket cards |
| `bg_card_green.xml` | Conference/Sports event/ticket cards |
| `bg_chip_selected.xml` | Active category chip (purple) |
| `bg_chip_default.xml` | Inactive category chip |
| `bg_tab_selected.xml` | Active tab (Upcoming) |
| `bg_tab_default.xml` | Inactive tab |
| `bg_price_chip.xml` | Amber price label |
| `bg_category_label.xml` | Semi-transparent category label on cards |
| `bg_active_badge.xml` | Amber "Active" badge on ticket cards |
| `bg_avatar.xml` | Oval purple avatar background |
| `bg_info_cell.xml` | Dark surface cell (info grid, settings rows, profile rows) |
| `bg_dashed_divider.xml` | Dashed horizontal line on ticket/booking cards |
| `bg_circle_outline.xml` | Circle outline on Private Event screen |
| `bg_qr_placeholder.xml` | QR code placeholder box on ticket cards |
| `bg_search.xml` | Rounded search bar background |
| `bg_logo_outer.xml` | Semi-transparent logo outer box |
| `bg_logo_inner.xml` | Solid purple logo inner box |
| `bg_nav_bar.xml` | Bottom nav background |
| `bg_admin_input.xml` | Rounded input field background on AdminActivity's event form |
| `ic_eye.xml` / `ic_eye_off.xml` | Password show/hide toggle icons (Login/Register password fields) |
| `ic_notification.xml` | Flat white ticket-silhouette icon used as the status-bar small icon for posted notifications |

Launcher icon (`ic_launcher_background.xml` + `ic_launcher_foreground.xml`, adaptive icon): solid `primary_purple` (#7B5CF6) background with an amber (#F5A623) ticket mark (rounded shape, punched stub notches, dashed perforation line) — replaced the default Android Studio robot icon. Legacy pre-API26 raster fallbacks in `mipmap-*dpi/ic_launcher*.webp` still show the old robot (only matters on Android 7.x devices, which don't support adaptive icons).

---

## Design System
| Token | Color | Used for |
|-------|-------|----------|
| `background_dark` | #0F0C1F | All screen backgrounds |
| `surface_dark` | #1A1635 | Cards, search bar, bottom nav, info cells |
| `card_purple` | #251F5C | Concert event cards |
| `card_green` | #1A3D2B | Conference/Sports event cards |
| `primary_purple` | #7B5CF6 | Buttons, active states, avatar, active nav |
| `amber` | #F5A623 | Price chips, Book button, Active badge |
| `text_muted` | #9B98B8 | Secondary text, inactive nav icons |
| `chip_bg` | #2A2650 | Unselected category chips |
| `error_red` | #CF6679 | Error messages, logout button text |

Theme: `Theme.MaterialComponents.DayNight.NoActionBar`, now genuinely day/night — toggled by the user (Settings → Dark mode), not by system setting. Screens reference semantic attrs (`?attr/colorAppBackground` etc., see Tech Stack) rather than the raw dark-palette colors directly; `res/values/colors.xml` holds both the original dark palette and a new `_light` suffixed palette used by `res/values/themes.xml`, while `res/values-night/themes.xml` maps the same attrs back to the dark palette.

---

## Database Schema
(`DatabaseHelper` DB_VERSION = 8 — `onUpgrade` just drops and recreates all tables, no migration/data preservation)

**users** — id, full_name, email (UNIQUE), `firebase_uid` (UNIQUE, nullable until first login/register), phone, **`role`** (`'USER'`/`'PROMOTER'`/`'ADMIN'`, default `'USER'` — replaced the old `is_admin` boolean). Passwords are no longer stored here — Firebase Auth owns credentials.
**events** — id, title, location, date, category, price, is_private, `image_url`, `time`, `slots`, `description`, **`organizer_id`** (nullable — the `USER` who requested it), **`promoter_id`** (nullable — whose hall hosts it), **`status`** (`'PENDING'`/`'PUBLISHED'`/`'REJECTED'`, default `'PUBLISHED'`), **`access_code`** (set only when a private event's request is approved)
**bookings** — id, user_id, event_id, ticket_number, `status` (default `PENDING`, becomes `COMPLETED` after payment), **`checked_in`** (default 0, flipped to 1 by `ScanEntryActivity`/`checkInTicket()` on first door scan)
**promoter_applications** *(new)* — id, user_id, hall_name, location, description, `status` (`'PENDING'`/`'APPROVED'`/`'REJECTED'`)

Admin-created events (via `AdminActivity`) and all seeded events get `status='PUBLISHED'` directly with `organizer_id`/`promoter_id` NULL — Admin bypasses the promoter-approval loop for its own posts. Events requested via `OrganizeEventActivity` start at `status='PENDING'` and are invisible to `getAllEvents()`/`searchEvents()` until a promoter approves them.

Seeded on first launch:
- 4 events (3 public + 1 private): Sauti Sol Live in DSM (Concert, Tsh 15,000), DSM Tech Conference 2025 (Conference, Tsh 30,000), Kariakoo Marathon 2025 (Sports, Tsh 5,000), Private Rooftop Party (Concert, Tsh 50,000, private — not shown in home feed)
- 1 admin profile row: `admin@gmail.com` (`role='ADMIN'`, `firebase_uid` null until linked). **The Firebase Auth account itself (`admin@gmail.com` / `123456`) must be created manually in the Firebase Console** under Authentication → Users — it does not exist just because the SQLite row is seeded.
- 1 local-only test user row: `normal@gmail.com` (`role='USER'`) — see the local-login bypass note under Important Notes.

### Key DB methods in DatabaseHelper
- `registerLocalProfile(fullName, email, firebaseUid, phone)` — inserts the local profile row after Firebase Auth account creation (`role='USER'`)
- `getUserByFirebaseUid(uid)` / `getUserByEmail(email)` / `getUserById(id)` / `linkFirebaseUid(localId, uid)` — used by `LoginActivity` to resolve/link the local profile to a Firebase account
- `submitPromoterApplication(userId, hallName, location, description)` / `getLatestPromoterApplication(userId)` / `getPendingPromoterApplications()` / `approvePromoterApplication(applicationId, userId)` (also flips `users.role` to `PROMOTER`) / `rejectPromoterApplication(applicationId)`
- `getApprovedPromoters()` — `List<PromoterProfile>` for the "choose a promoter" picker; `getPromoterProfile(promoterUserId)` — one promoter's approved hall info
- `getAllEvents()` / `searchEvents(query)` — public events only, **and now `status='PUBLISHED'` only**
- `getAllEventsForAdmin()` — no filters at all, for Admin's monitoring view
- `addEvent(...)` — admin event creation, `status='PUBLISHED'` directly, now also generates an `access_code` (entry QR) like the organizer-approval path
- `requestEvent(..., organizerId, promoterId)` — organizer's "organize event" submission, `status='PENDING'`
- `getPendingEventRequestsForPromoter(promoterId)` — `List<EventRequest>` (event + organizer name) for the promoter dashboard
- `approveEventRequest(eventId, finalPrice, finalDate, finalTime, isPrivate)` — promoter's Accept: updates price/date/time, `status='PUBLISHED'`, **always** generates an `access_code` (public or private — used as the event's entry QR, not just a private-listing gate)
- `rejectEventRequest(eventId)` — `status='REJECTED'`
- `getEventsByOrganizer(organizerId)` — all statuses, for "My Organized Events"
- `getEventByAccessCode(code)` — used by `PrivateEventActivity`; matches any `status='PUBLISHED'` event (public or private) — no longer gated on `is_private=1`
- `deleteEvent(eventId)` — admin event deletion
- `bookEvent(userId, eventId)` — generates BKF-XXXX-XXXX ticket, inserts booking with `status='PENDING'`
- `completePayment(userId, eventId)` — flips a booking's status to `COMPLETED` after Mongike payment succeeds
- `isAlreadyBooked(userId, eventId)` — duplicate check (any status)
- `getTicketNumber(userId, eventId)` — retrieve ticket number, **only for `COMPLETED` bookings**
- `getUserBookings(userId)` — JOIN bookings+events, **only `COMPLETED`** bookings, returns List<Booking>
- `updateUserName(userId, fullName)` — used by `EditProfileActivity` to rename a user's local profile row

---

## Roles & Event-Organizing Flow
Three roles, `users.role`: `USER` (default), `PROMOTER`, `ADMIN`. Registration never assigns anything but `USER` — Promoter status is requested afterward and Admin-only accounts are seeded/manually granted, there's no self-service admin signup.

**Becoming a Promoter:**
1. From `ProfileActivity`, a `USER` taps "Become a Promoter" → `BecomePromoterActivity` → fills hall/venue name, location, description → `submitPromoterApplication()`, row status `PENDING`.
2. Profile's promoter row now reads "Promoter application pending review" (no action) until Admin decides.
3. Admin reviews pending applications in `AdminActivity` → Approve flips `users.role` to `PROMOTER` (and the application row to `APPROVED`) / Reject flips it to `REJECTED` (user can resubmit — old rejected rows are just left in place, not deleted).
4. Next login, a `PROMOTER` routes straight to `PromoterDashboardActivity` instead of `HomeFeedActivity`.

**Organizing an event (any `USER` or `PROMOTER`, via the Home FAB → `OrganizeEventActivity`):**
1. Pick an approved promoter/hall from a Spinner (`getApprovedPromoters()`), fill the event form (title, category, date, time, proposed price, slots, description, image), toggle public/private.
2. Submit → `requestEvent(...)` inserts an `events` row with `status='PENDING'`, `organizer_id`=me, `promoter_id`=chosen hall. **Not visible anywhere yet** — `getAllEvents()`/`searchEvents()` filter on `status='PUBLISHED'`.
3. The target Promoter sees it in `PromoterDashboardActivity`'s pending-requests list (organizer name + event details) and can:
   - **Accept** — a dialog lets them adjust price/date/time before confirming; `approveEventRequest()` sets `status='PUBLISHED'` (and generates an `access_code` if the event is private).
   - **Reject** — `status='REJECTED'`; the organizer sees this in `MyEventRequestsActivity` and the event never appears publicly.
4. **Public + approved** → shows up in the normal home feed/explore/search immediately, bookable through the existing `EventDetailActivity → PaymentActivity → TicketDetailActivity` pipeline, completely unchanged.
5. **Private + approved** → still excluded from public listings (`is_private=1` is already filtered out of `getAllEvents()`), but the organizer's `MyEventRequestsActivity` now shows a "Share Invite Code" button that opens the Android share sheet (`ACTION_SEND`, `text/plain`) with a formatted invite message containing the `access_code` — meant to be sent manually via WhatsApp/SMS/etc., like a paper invitation. An invitee enters that code in `PrivateEventActivity` → `getEventByAccessCode()` looks it up → on match, launches `EventDetailActivity` with the same extras `HomeFeedActivity` builds for a normal event card, dropping them into the identical booking/payment flow for that one event.

**Admin** approves/rejects Promoter applications and — via `getAllEventsForAdmin()` — sees every event regardless of status/visibility (PENDING/PUBLISHED/REJECTED, public or private) in one list with a status badge, i.e. "monitors everything." Admin-posted events still bypass the request/approval loop entirely (`addEvent()` → `status='PUBLISHED'` directly).

---

## Booking + Payment Logic (EventDetailActivity → PaymentActivity → TicketDetailActivity)
1. Read `user_id` from `"bookify_session"` SharedPreferences; read `event_id` from Intent extra
2. If not logged in → show dialog
3. If already booked and `COMPLETED` → show existing ticket number (dialog with "View My Tickets")
4. If booked but still `PENDING` (payment never finished) → go straight to PaymentActivity
5. If new booking → `db.bookEvent()` inserts a `PENDING` row with a generated BKF-XXXX-XXXX ticket number → launches `PaymentActivity`
6. `PaymentActivity` collects a phone number, POSTs to the Mongike API (`https://mongike.com/api/v1/payments/mobile-money/tanzania`), then **mock-verifies** the payment (`checkMongikeAccountStatus` always returns `true` — no real webhook/status polling yet) and calls `db.completePayment()`
7. On success (`RESULT_OK`), `EventDetailActivity.onActivityResult` looks up the ticket number and launches `TicketDetailActivity`, which renders a ZXing QR code encoding the ticket number
8. A booking only becomes visible in `MyTicketsActivity` once its status is `COMPLETED`

---

## Full Navigation Map
```
HomeFeedActivity (Launcher — always the first screen, logged in or not)
  ├── Guest: greeting falls back to "Guest"; FAB and event-card taps show
  │          AuthGate's Log In/Create Account prompt instead of navigating
  ├── Avatar circle (top right) → ProfileActivity
  ├── Event card tap (logged in) → EventDetailActivity
  ├── FAB (fab_action, role-aware, logged in) → AdminActivity (ADMIN) / PromoterDashboardActivity (PROMOTER) / OrganizeEventActivity (USER)
  ├── Explore bottom nav        → ExploreActivity
  ├── Tickets bottom nav        → MyTicketsActivity
  └── Profile bottom nav        → ProfileActivity

MainActivity (Splash — no longer the launcher; reached via explicit navigation, e.g. logout)
  ├── "Get started" → RegisterActivity
  └── "Log in"      → LoginActivity
        ├── (success, role=USER)     → HomeFeedActivity [clears stack]
        ├── (success, role=PROMOTER) → PromoterDashboardActivity [clears stack]
        └── (success, role=ADMIN)    → AdminActivity [clears stack]

AuthGate prompt (Home/Explore event tap or Home FAB, guest only)
  ├── "Log In"          → LoginActivity (carries the tapped event, if any, as an extra)
  ├── "Create Account"  → RegisterActivity (same)
  └── "Cancel"           → dismiss, stay put

  On success, if a pending event extra is present: HomeFeedActivity is pushed as the
  task root, then EventDetailActivity for that event on top — so the user lands
  straight back on the event they tapped, and back navigation still works normally.

OrganizeEventActivity (any logged-in user, reached via the Home FAB)
  ├── Submit request → MyEventRequestsActivity [finishes]
  └── ← back         → previous screen (finish)

MyEventRequestsActivity
  ├── "Share Invite Code" (published private events) → Android share sheet
  └── ← back → ProfileActivity

BecomePromoterActivity (reached from ProfileActivity's promoter row)
  ├── Submit → back to ProfileActivity [finish]
  └── ← back → ProfileActivity

PromoterDashboardActivity (role=PROMOTER only, reached via login or the Home FAB)
  ├── Accept request (dialog: adjust price/date/time) → event published
  ├── Reject request                                  → event rejected
  ├── "Logout"        → LoginActivity [clears session]
  └── "View as User"  → HomeFeedActivity

ExploreActivity
  ├── Event card tap   → EventDetailActivity
  ├── Home bottom nav  → HomeFeedActivity
  ├── Tickets nav      → MyTicketsActivity
  └── Profile nav      → ProfileActivity

EventDetailActivity
  ├── ← back          → previous screen (finish)
  └── "Book Ticket"   → PaymentActivity (startActivityForResult)
        └── (payment success) → TicketDetailActivity [finishes EventDetailActivity]

PaymentActivity
  └── (success) → returns RESULT_OK to EventDetailActivity

TicketDetailActivity
  └── ← back → previous screen (finish)

MyTicketsActivity
  ├── Ticket card tap → TicketDetailActivity (QR code)
  ├── "🔒 Private"    → PrivateEventActivity
  ├── Home nav        → HomeFeedActivity
  └── Profile nav     → ProfileActivity

PrivateEventActivity
  ├── Home nav        → HomeFeedActivity
  ├── Tickets nav     → MyTicketsActivity
  └── Profile nav     → ProfileActivity

ProfileActivity
  ├── "My Bookings"          → MyBookingsActivity
  ├── "My Organized Events"  → MyEventRequestsActivity
  ├── Promoter row           → BecomePromoterActivity (USER, no pending app) / nothing (pending) / PromoterDashboardActivity (PROMOTER) / hidden (ADMIN)
  ├── "Settings"             → SettingsActivity
  ├── Logout                 → MainActivity [clears stack, clears SharedPreferences]
  ├── Home nav               → HomeFeedActivity
  └── Tickets nav            → MyTicketsActivity

MyBookingsActivity
  └── ← back         → ProfileActivity

SettingsActivity
  └── ← back         → ProfileActivity

AdminActivity (role=ADMIN only, reached via login or the Home FAB)
  ├── Image picker (gallery)         → attaches image to new event
  ├── "Save"                         → adds event to DB (status=PUBLISHED), refreshes list
  ├── Approve/Reject promoter app    → flips users.role / application status
  ├── Delete button per row          → removes event from DB
  ├── "Logout"                       → LoginActivity [clears session]
  └── "View as User"                 → HomeFeedActivity
```

---

## Screens vs Mockups Status
| # | Mockup | Status |
|---|--------|--------|
| 1 | Splash screen | ✅ Built |
| 2 | Home feed | ✅ Built |
| 3 | Event detail | ✅ Built + booking + payment logic |
| 4 | Private event access | ✅ Built (UI only, no real validation) |
| 5 | My tickets | ✅ Built (DB-backed, `COMPLETED` bookings only) |
| 6 | Register | ✅ Built (redesigned dark theme) |
| 7 | Login | ✅ Built (redesigned dark theme, admin routing) |
| 8 | Explore | ✅ Built (live search) |
| 9 | My Bookings | ✅ Built (DB-backed) |
| 10 | Profile | ✅ Built |
| 11 | Settings | ✅ Built |
| — | Admin Dashboard | ✅ Built (post/delete events, image upload) |
| — | Payment (Mongike) | ✅ Built (mock-verified, see Important Notes) |
| — | Ticket Detail / QR | ✅ Built (ZXing QR generation; no camera scanning yet) |

---

## Pending / Next Steps
- [x] ~~Users can't edit their profile name~~ Done — `EditProfileActivity`, see Tech Stack/Activities. Email/phone deliberately left non-editable (email is tied to Firebase Auth identity; scope decision made this session)
- [x] ~~No dark/light mode or Swahili translation~~ Done — see Tech Stack ("Dark/light mode" and "Language" entries). Note: `MapActivity`'s translucent-scrim map hint and QR-code white backdrops were deliberately left as fixed white/black — they're not theme surfaces, they're functional overlays (legibility over live map tiles / QR scannability)
- [x] ~~No way to share a private event invite via WhatsApp specifically~~ Done — see Tech Stack ("QR Codes"/"Sharing" entries) and `MyEventRequestsActivity`/`AdminActivity`. Every PUBLISHED event (not just private ones) now gets an entry QR, shareable via WhatsApp or the generic share sheet.
- [ ] **No in-app QR *scanning* for door entry** — `InviteShareHelper` generates and shares the entry QR, but there's still no camera-based scan screen for organizers to validate it against the DB (mark an attendee "checked in", prevent duplicate entry, etc.) — same gap as the pre-existing ticket-QR scanning TODO below, now doubly relevant. Would need a new dependency (e.g. `journeyapps:zxing-android-embedded` or CameraX + ML Kit) plus a DB column to track redemption.
- [ ] PaymentActivity — `checkMongikeAccountStatus()` is mocked (always returns `true`); needs a real webhook or status-polling integration against Mongike
- [ ] **Rotate the Mongike API key** — it was committed in plaintext to `PaymentActivity.java` in commit `0532675` and is permanently exposed in git history even though it's now moved to `local.properties`/`BuildConfig` (see Important Notes)
- [x] ~~PrivateEventActivity — real access code validation against DB~~ Done — wired to `getEventByAccessCode()`, launches `EventDetailActivity` on match
- [ ] activity_home_feed.xml — the static "Good morning," label above tv_greeting is hardcoded; make it dynamic
- [ ] GPS integration for nearby event filtering
- [ ] TicketDetailActivity — no actual QR *scanning*/entry-validation flow yet, despite being described as a scan/validation screen
- [ ] `onUpgrade()` in DatabaseHelper drops all tables (users/events/bookings/promoter_applications wiped on every schema bump) — fine for coursework, but note before shipping. DB_VERSION is now 8; anyone with the app already installed loses local data (bookings, custom events, promoter applications) on next launch.
- [ ] Create the `admin@gmail.com` / `123456` account in the Firebase Console (Authentication → Users) — admin login is broken without it, since credentials now live in Firebase Auth, not SQLite
- [ ] Firebase Analytics dependency is included but not actually invoked anywhere in code yet (auto-init only)
- [ ] **Real Firebase Auth login/registration is not working yet** — most likely cause: the "Email/Password" sign-in provider isn't enabled under Authentication → Sign-in method in the Firebase Console for project `bookify-461a7`. `RegisterActivity`'s failure Toast now surfaces `e.getMessage()`, so check that text in-app to confirm the exact error. Until it's fixed, use the `normal@gmail.com` local test login above.
- [ ] A rejected Promoter application can be resubmitted, but there's no UI to show application *history* — `getLatestPromoterApplication()` only surfaces the most recent row, older ones are invisible in the app (still in the DB)
- [ ] `OrganizeEventActivity`'s event `location` is auto-set to the chosen promoter's registered location (not separately editable) — intentional (you're booking *their* hall), but worth confirming that's the intended semantics if hall setups get more complex later
- [x] ~~No in-app notification when a promoter accepts/rejects a request or an admin approves/rejects a promoter application~~ Done — real Android notifications now fire from `PromoterDashboardActivity`/`AdminActivity` via `NotificationHelper` (see Tech Stack). Note these still only notify the device the action was taken on (single local SQLite DB, no backend push) — a cross-device notify-the-other-user flow would need Firebase Cloud Messaging
- [ ] Promoters organize events only by approving other users' requests — there's no shortcut for a Promoter to self-publish an event on their own hall without going through their own request/approval loop
- [ ] `MAPS_API_KEY` is not set in `local.properties` — `MapActivity` builds and launches fine but won't render real map tiles until a real Google Maps API key is added
- [ ] Legacy pre-API26 launcher icon raster files (`mipmap-*dpi/ic_launcher*.webp`) still show the old default Android robot — only affects Android 7.x devices (no adaptive icon support)

---

## Important Notes
- `android:layout_marginHorizontal` requires API 26+ — use `layout_marginLeft` + `layout_marginRight` instead (minSdk is 24)
- `android:paddingHorizontal` same issue — use `paddingLeft` + `paddingRight`
- Never use `.kt` files — Java only
- **Security: Mongike API key.** Originally hardcoded in `PaymentActivity.java` and committed to the public GitHub repo (commit `0532675`). Moved to `local.properties` (gitignored) → exposed via `BuildConfig.MONGIKE_API_KEY`, set in `app/build.gradle.kts`. The key is still exposed in git history from the earlier commit — **rotate it in the Mongike dashboard.** Anyone cloning the repo now needs their own `MONGIKE_API_KEY=...` line in `local.properties` for `PaymentActivity` to work.
- **Git history was rewritten on `master` (2026-07-27).** The commit "Update CLAUDE.md for payments/admin/QR features; move Mongike API key out of source" (originally `ffe8b846`, now `c600654`) had accidentally picked up a `Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>` trailer, which made GitHub attribute Claude as a repo contributor. That trailer was stripped via `git filter-branch` and the rewritten history was force-pushed to `origin/master` — no file content changed, only that commit's message. **Anyone with an existing local clone from before this rewrite needs to run `git fetch origin && git reset --hard origin/master`** (after stashing/branching any local work) since their local `master` now diverges from the remote.
- **Firebase setup.** `app/google-services.json` is required for the app to build/run (the `google-services` Gradle plugin reads it) but is gitignored and **not committed** — it was never pushed to the repo, so there's no history-exposure issue like the Mongike key. Anyone cloning the repo (or setting up a new machine) needs to download their own `google-services.json` from the Firebase Console (project `bookify-461a7`, Android package `com.example.bookify`) and drop it in `app/`, otherwise the build fails at `processDebugGoogleServices`.
- Seeded admin login for testing: `admin@gmail.com` / `123456` — the SQLite row is seeded automatically, but the matching Firebase Auth account must be created manually in the Firebase Console (Authentication → Users) before admin login will work.
- **Local-only test login (bypasses Firebase entirely):** `normal@gmail.com` / any password, phone `0695880700`. `LoginActivity.attemptLogin()` special-cases `DatabaseHelper.TEST_USER_EMAIL` and logs straight into the seeded `Normal User` SQLite row without calling `FirebaseAuth` — added because real Firebase login/registration wasn't working end-to-end yet (see Pending). Seeded via `DatabaseHelper.seedTestUser()`, DB_VERSION bumped to 7 to force reseed on existing installs.
- **Maps API key.** Like `MONGIKE_API_KEY`, `MapActivity` reads `MAPS_API_KEY` from `local.properties` (gitignored) via `BuildConfig.MAPS_API_KEY` and a manifest placeholder (`com.google.android.geo.API_KEY`). Anyone cloning the repo needs their own `MAPS_API_KEY=...` line in `local.properties` for the map to actually render tiles — without it the app still builds/runs, the map view is just blank.
- **Watch merge conflicts in `app/build.gradle.kts` and `AndroidManifest.xml` closely.** A prior merge (combining the Firebase-auth branch with the Map-feature branch) resolved conflicts in both files by silently dropping content from *both* sides instead of keeping it — this deleted the `google-services` plugin, the Firebase/Maps dependencies, the `ACCESS_NETWORK_STATE` permission, and four activity declarations, and broke the build entirely until caught and fixed. When resolving a conflict in either file, diff both parent versions explicitly rather than trusting the auto-merge.
- Notifications require the app to actually be granted `POST_NOTIFICATIONS` (Android 13+ default is denied/blocked until the runtime permission dialog is accepted) — if notifications don't seem to be firing, check the app's system notification settings first (`Settings → Apps → Bookify → Notifications`), not just the in-app toggle.
- Always update this file and `D:\Bookify context\CHANGES.md` after changes