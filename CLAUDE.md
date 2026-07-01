# Bookify — Android App Context

## What This App Is
Bookify is an Android event booking app for Tanzania.
- **Group:** DIT Group No. 8, class OD23IT
- **Team:** Sunday Julius Kihiyo, Sarah John Mahwera, Omary Isihaka Athuman, Yusuph Samwel Mniko, Peter Ladislaus Msimbira

---

## Language & Tech Stack
- **Language: Java only — NOT Kotlin.** Do not add any `.kt` files.
- **UI: XML Views** (no Jetpack Compose)
- **Database: SQLite** via `DatabaseHelper.java`
- **UI Libraries:** AppCompat, Material Components, RecyclerView, CardView, ConstraintLayout

---

## App Structure

### Activities (Flow)
```
MainActivity (Splash)
  ├── "Get started" → RegisterActivity
  └── "Log in"      → LoginActivity
                         └── HomeFeedActivity (after successful login/register)
```

### Source Files
| File | Purpose |
|------|---------|
| `MainActivity.java` | Splash screen with logo, tagline, Get Started + Log In buttons |
| `LoginActivity.java` | Email + password login; validates against SQLite via DatabaseHelper |
| `RegisterActivity.java` | Full name, email, password, phone registration with uniqueness check |
| `HomeFeedActivity.java` | Home feed: dynamic greeting, avatar initials, category chips, events RecyclerView, bottom nav bar |
| `data/DatabaseHelper.java` | SQLite helper — creates users, events, bookings tables; seeds 4 events on first launch |
| `data/User.java` | POJO: id, fullName, email, phone |
| `data/Event.java` | POJO: id, title, location, date, category, price, isPrivate |
| `adapter/EventAdapter.java` | RecyclerView adapter for event cards; supports category filtering + click listener |

### XML Layouts
| File | Screen |
|------|--------|
| `activity_main.xml` | Splash — logo, title, tagline, buttons, dot page indicators |
| `activity_login.xml` | Login — email/password fields, error label, sign-up link |
| `activity_register.xml` | Register — full name, email, password, phone fields |
| `activity_home_feed.xml` | Home feed — header, search bar, category chips, RecyclerView, bottom nav |
| `item_event_card.xml` | Single event card — title, location/date, category chip, price chip |

### Drawables
Background shapes used across the app:
`bg_card_purple`, `bg_card_green`, `bg_chip_selected`, `bg_chip_default`,
`bg_price_chip`, `bg_category_label`, `bg_avatar`, `bg_button_primary`,
`bg_logo_outer`, `bg_logo_inner`, `bg_search`, `bg_nav_bar`

---

## Design System
| Token | Color | Used for |
|-------|-------|----------|
| `background_dark` | #0F0C1F | All screen backgrounds |
| `surface_dark` | #1A1635 | Cards, search bar, bottom nav |
| `card_purple` | #251F5C | Concert event cards |
| `card_green` | #1A3D2B | Conference/Sports event cards |
| `primary_purple` | #7B5CF6 | Buttons, active states, avatar |
| `amber` | #F5A623 | Price chips |
| `text_muted` | #9B98B8 | Secondary text, inactive nav icons |
| `chip_bg` | #2A2650 | Unselected category chips |

Theme: `Theme.MaterialComponents.DayNight.NoActionBar` (always dark)

---

## Database Schema

**users** — id, full_name, email (UNIQUE), password, phone  
**events** — id, title, location, date, category, price, is_private  
**bookings** — id, user_id, event_id, ticket_number  

Seeded events (4 total, 3 public + 1 private):
1. Sauti Sol Live in DSM — Concert — Tsh 15,000
2. DSM Tech Conference 2025 — Conference — Tsh 30,000
3. Kariakoo Marathon 2025 — Sports — Tsh 5,000
4. Private Rooftop Party — Concert — Tsh 50,000 (private)

---

## Screens Designed (Mockups in D:\Bookify context\)
1. Splash screen ✅
2. Home feed ✅
3. Event detail ✅
4. Private event access (invite code) ✅
5. My tickets ✅
6. Register ✅
7. Login ✅
8. Profile ✅

## Navigation Map
```
MainActivity (Splash)
  ├── Get started → RegisterActivity → HomeFeedActivity
  └── Log in      → LoginActivity   → HomeFeedActivity

HomeFeedActivity
  ├── Event card tap  → EventDetailActivity (passes title/location/date/price)
  ├── Tickets nav     → MyTicketsActivity
  └── Profile nav     → ProfileActivity

MyTicketsActivity
  ├── 🔒 Private link → PrivateEventActivity
  ├── Home nav        → HomeFeedActivity
  └── Profile nav     → ProfileActivity

PrivateEventActivity
  ├── Home nav        → HomeFeedActivity
  ├── Tickets nav     → MyTicketsActivity
  └── Profile nav     → ProfileActivity

ProfileActivity
  ├── Logout          → MainActivity (clears stack)
  ├── Home nav        → HomeFeedActivity
  └── Tickets nav     → MyTicketsActivity
```

---

## Pending / Next Steps
- [ ] Real booking logic in EventDetailActivity (Book Ticket button)
- [ ] Access code validation in PrivateEventActivity
- [ ] Replace hardcoded ticket cards in MyTicketsActivity with DB-backed RecyclerView
- [ ] Replace hardcoded greeting "Good morning," in activity_home_feed.xml with dynamic time-based label
- [ ] GPS integration for nearby event filtering

---

## Files to Always Update After Changes
- `D:\Bookify context\CHANGES.md` — append a dated entry after every session
