# Bookify — Android App Context

## What This App Is
Bookify is an Android event booking app for Tanzania.
- **Group:** DIT Group No. 8, class OD23IT
- **Team:** Sunday Julius Kihiyo, Sarah John Mahwera, Omary Isihaka Athuman, Yusuph Samwel Mniko, Peter Ladislaus Msimbira

---

## Language & Tech Stack
- **Language: Java only — NOT Kotlin.** Never add `.kt` files.
- **UI: XML Views** — no Jetpack Compose
- **Database: SQLite** via `DatabaseHelper.java` (schema version 5)
- **Session: SharedPreferences** — key `"bookify_session"`, stores `user_id` (int), `user_name`, `user_email`, `is_admin` (boolean). Saved on login/register, cleared on logout.
- **UI Libraries:** AppCompat, Material Components, RecyclerView, CardView, ConstraintLayout
- **QR Codes:** ZXing (`com.google.zxing:core:3.5.3`) — generates QR bitmaps from ticket numbers, drawn in-app (no camera/scanning yet)
- **Payments:** Mongike mobile money gateway (Tanzania: M-Pesa, Tigo, Airtel, Halopesa) — called directly from `PaymentActivity` via `HttpURLConnection`
- **Permissions:** `INTERNET` (added in `AndroidManifest.xml` for the payment API call)

---

## All Source Files

### Activities
| File | Purpose |
|------|---------|
| `MainActivity.java` | Splash screen — logo, tagline, "Get started" → Register, "Log in" → Login |
| `LoginActivity.java` | Email + password login; validates via DB; saves session (incl. `is_admin`); routes admins → AdminActivity, regular users → HomeFeedActivity |
| `RegisterActivity.java` | Full name, email, password (min 6 chars, confirm match), phone; uniqueness check; saves session |
| `HomeFeedActivity.java` | Home feed — dynamic greeting, avatar (tappable → Profile), category chips, events RecyclerView, bottom nav; shows a FAB → AdminActivity if `is_admin` |
| `ExploreActivity.java` | Browse + live search all events (filters as user types); bottom nav Explore active |
| `EventDetailActivity.java` | Event detail — hero card (with image if set), 4-cell info grid, About text; Book Ticket → creates a `PENDING` booking → launches `PaymentActivity` → on success routes to `TicketDetailActivity` to show the QR code |
| `MyTicketsActivity.java` | My tickets — now DB-backed (`db.getUserBookings`) via `BookingAdapter`; only shows `COMPLETED` (paid) bookings; empty state if none; "🔒 Private" link; bottom nav Tickets active |
| `PrivateEventActivity.java` | Private event — access code + paste-link inputs, hardcoded private bookings list; bottom nav Tickets active |
| `MyBookingsActivity.java` | My bookings — DB-backed list of current user's bookings via BookingAdapter; shows empty state if none |
| `ProfileActivity.java` | Profile — loads name/email/initials from SharedPreferences; My Bookings + Settings rows; logout clears session |
| `SettingsActivity.java` | Settings — push notification + email reminder toggles (persisted in `"bookify_settings"` prefs); About section |
| `AdminActivity.java` | Admin dashboard (admin users only) — form to post new events (title, location, date, category, price, time, slots, description, gallery image picker via `ACTION_OPEN_DOCUMENT`); RecyclerView of existing events with delete button (`AdminEventAdapter`); "Logout" and "View as User" links |
| `PaymentActivity.java` | Mongike mobile-money checkout — collects phone number, POSTs to the Mongike API, mock-verifies the payment, then calls `db.completePayment()` to flip the booking to `COMPLETED` and returns `RESULT_OK` |
| `TicketDetailActivity.java` | Shows a booked ticket — title, info, ticket number, event image (if any), and a ZXing-generated QR code encoding the ticket number |

### Data Layer
| File | Purpose |
|------|---------|
| `data/DatabaseHelper.java` | SQLite (schema v5) — users, events, bookings tables; seeds 4 events + 1 admin user; CRUD/search/login/register/payment methods |
| `data/User.java` | POJO: id, fullName, email, phone, `isAdmin` |
| `data/Event.java` | POJO: id, title, location, date, category, price, isPrivate, `imageUrl`, `time`, `slots`, `description` |
| `data/Booking.java` | POJO: ticketNumber, eventTitle, eventDate, eventCategory, eventPrice, `imageUrl`, `status` (`PENDING`/`COMPLETED`) |

### Adapters
| File | Purpose |
|------|---------|
| `adapter/EventAdapter.java` | Event cards RecyclerView — supports `filter(category)`, `search(query)`, `setOnEventClickListener`; shows event image if `imageUrl` set, else emoji icon |
| `adapter/BookingAdapter.java` | Booking cards RecyclerView — used in MyTicketsActivity/MyBookingsActivity; shows event image if set; tapping a card opens `TicketDetailActivity` (QR code) |
| `adapter/AdminEventAdapter.java` | Admin event-list RecyclerView — shows title/location/date/image per event with a delete button (`OnDeleteClickListener`) |

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
| `activity_private_event.xml` | Private event — circle icon, access code + link inputs, hardcoded bookings, bottom nav |
| `activity_my_bookings.xml` | My bookings — back button, empty state TextView, RecyclerView |
| `activity_profile.xml` | Profile — avatar, name/email, My Bookings row, Settings row, logout button, bottom nav |
| `activity_settings.xml` | Settings — back button, notification toggles (SwitchCompat), About rows |
| `activity_admin.xml` | Admin dashboard — event form fields (title, location, date, category, price, time, slots, description), image picker preview, Save button, RecyclerView of existing events, logout/view-as-user links |
| `activity_payment.xml` | Payment — amount display, phone number input, "Pay Now" button, progress bar |
| `activity_ticket_detail.xml` | Ticket detail — title, info, ticket number, event image, QR code image, back button |
| `item_event_card.xml` | Event card — image (or emoji fallback), title, location/date, category chip, price chip |
| `item_booking_card.xml` | Booking card — event image, category, title, date+price, dashed divider, ticket number + QR placeholder |
| `item_admin_event.xml` | Admin event row — thumbnail, title, location/date info, delete button |

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

Theme: `Theme.MaterialComponents.DayNight.NoActionBar` (always dark)

---

## Database Schema
(`DatabaseHelper` DB_VERSION = 5 — `onUpgrade` just drops and recreates all tables, no migration/data preservation)

**users** — id, full_name, email (UNIQUE), password, phone, `is_admin` (default 0)
**events** — id, title, location, date, category, price, is_private, `image_url`, `time`, `slots`, `description`
**bookings** — id, user_id, event_id, ticket_number, `status` (default `PENDING`, becomes `COMPLETED` after payment)

Seeded on first launch:
- 4 events (3 public + 1 private): Sauti Sol Live in DSM (Concert, Tsh 15,000), DSM Tech Conference 2025 (Conference, Tsh 30,000), Kariakoo Marathon 2025 (Sports, Tsh 5,000), Private Rooftop Party (Concert, Tsh 50,000, private — not shown in home feed)
- 1 admin user: `admin@gmail.com` / `123456` (`is_admin = 1`)

### Key DB methods in DatabaseHelper
- `registerUser()` / `loginUser()` (returns `is_admin`) / `emailExists()`
- `getAllEvents()` — public events only
- `searchEvents(query)` — LIKE search on title/location/category
- `addEvent(title, location, date, category, price, isPrivate, imageUrl, time, slots, description)` — admin event creation
- `deleteEvent(eventId)` — admin event deletion
- `bookEvent(userId, eventId)` — generates BKF-XXXX-XXXX ticket, inserts booking with `status='PENDING'`
- `completePayment(userId, eventId)` — flips a booking's status to `COMPLETED` after Mongike payment succeeds
- `isAlreadyBooked(userId, eventId)` — duplicate check (any status)
- `getTicketNumber(userId, eventId)` — retrieve ticket number, **only for `COMPLETED` bookings**
- `getUserBookings(userId)` — JOIN bookings+events, **only `COMPLETED`** bookings, returns List<Booking>

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
MainActivity (Splash)
  ├── "Get started" → RegisterActivity
  └── "Log in"      → LoginActivity
        ├── (success, regular user) → HomeFeedActivity [clears stack]
        └── (success, is_admin)     → AdminActivity [clears stack]

HomeFeedActivity
  ├── Avatar circle (top right) → ProfileActivity
  ├── Event card tap            → EventDetailActivity
  ├── FAB (admin only)          → AdminActivity
  ├── Explore bottom nav        → ExploreActivity
  ├── Tickets bottom nav        → MyTicketsActivity
  └── Profile bottom nav        → ProfileActivity

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
  ├── "My Bookings"   → MyBookingsActivity
  ├── "Settings"      → SettingsActivity
  ├── Logout          → MainActivity [clears stack, clears SharedPreferences]
  ├── Home nav        → HomeFeedActivity
  └── Tickets nav     → MyTicketsActivity

MyBookingsActivity
  └── ← back         → ProfileActivity

SettingsActivity
  └── ← back         → ProfileActivity

AdminActivity (admin users only, reached via login or the Home FAB)
  ├── Image picker (gallery) → attaches image to new event
  ├── "Save"                 → adds event to DB, refreshes list
  ├── Delete button per row  → removes event from DB
  ├── "Logout"               → MainActivity [clears session]
  └── "View as User"         → HomeFeedActivity
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
- [ ] PaymentActivity — `checkMongikeAccountStatus()` is mocked (always returns `true`); needs a real webhook or status-polling integration against Mongike
- [ ] **Rotate the Mongike API key** — it was committed in plaintext to `PaymentActivity.java` in commit `0532675` and is permanently exposed in git history even though it's now moved to `local.properties`/`BuildConfig` (see Important Notes)
- [ ] PrivateEventActivity — real access code validation against DB
- [ ] activity_home_feed.xml — the static "Good morning," label above tv_greeting is hardcoded; make it dynamic
- [ ] GPS integration for nearby event filtering
- [ ] TicketDetailActivity — no actual QR *scanning*/entry-validation flow yet, despite being described as a scan/validation screen
- [ ] `onUpgrade()` in DatabaseHelper drops all tables (users/events/bookings wiped on every schema bump) — fine for coursework, but note before shipping

---

## Important Notes
- `android:layout_marginHorizontal` requires API 26+ — use `layout_marginLeft` + `layout_marginRight` instead (minSdk is 24)
- `android:paddingHorizontal` same issue — use `paddingLeft` + `paddingRight`
- Never use `.kt` files — Java only
- **Security: Mongike API key.** Originally hardcoded in `PaymentActivity.java` and committed to the public GitHub repo (commit `0532675`). Moved to `local.properties` (gitignored) → exposed via `BuildConfig.MONGIKE_API_KEY`, set in `app/build.gradle.kts`. The key is still exposed in git history from the earlier commit — **rotate it in the Mongike dashboard.** Anyone cloning the repo now needs their own `MONGIKE_API_KEY=...` line in `local.properties` for `PaymentActivity` to work.
- Seeded admin login for testing: `admin@gmail.com` / `123456`
- Always update this file and `D:\Bookify context\CHANGES.md` after changes