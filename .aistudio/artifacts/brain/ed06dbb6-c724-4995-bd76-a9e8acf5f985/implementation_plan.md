# GitHub Personal Access Token (PAT) Integration & Live Remote Sync

Streamlined token management and remote sync for GitHub repository credentials in Creator Studio, enabling creators to seamlessly paste, generate, validate, and persist their GitHub Personal Access Token for automated data commits.

## User Review & Critical Decisions

> [!IMPORTANT]
> The GitHub Personal Access Token (PAT) input field allows creators to push live commits directly to their repository (`punithjr2/creator-studio-content`). The enhancements provide a 1-tap clipboard paste, password visibility toggle, direct token creation shortcut, and automated sync.

- **Confirmed Decision 1**: Add 1-tap "Paste from Clipboard" action next to the PAT input so long `ghp_...` tokens can be added without manual typing.
- **Confirmed Decision 2**: Add a direct "Generate Token on GitHub" action button that opens GitHub's PAT creator pre-filled with the necessary `repo` scope.
- **Confirmed Decision 3**: Provide token show/hide toggle and a "Verify & Sync Now" action to immediately validate credentials and push all studio projects, tasks, and scripts to the repository.

---

## 1. Overview & Core Concept

- **What It Does**: Enhances the GitHub Data Link screen with streamlined Personal Access Token input: one-tap clipboard paste, visibility toggle, direct web shortcut to create a token with repo permissions, token format validation (`ghp_...` or classic), credential persistence in Room DB, and automated remote sync.
- **Target Audience / Persona**: Content creators who want their scripts, YouTube/Instagram schedules, and video project metadata backed up directly into their personal GitHub repository.
- **Key Value**: Eliminates friction in obtaining and entering GitHub credentials on mobile and desktop, transitioning from local simulation commits to real live GitHub commits.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Enter / Paste Token**: Creator opens GitHub Data Link screen. Next to the PAT field, a "Paste" icon button automatically pulls the token from the system clipboard.
  2. **View / Hide Token**: A visibility eye icon toggles between masked (`••••••••`) and plain text for verification.
  3. **Generate Token Shortcut**: Clicking "Generate Token" opens GitHub's new token settings page pre-populated with `repo` scope.
  4. **Save & Sync**: Tapping "Save & Sync Now" validates token syntax, stores it securely in `CreatorSettingsEntity` via Room DB, and immediately triggers `GitHubRepository.syncProjectToRepo` with live commit feedback.

- **Visual Identity & Theme**:
  - Dark sleek GitHub-themed palette (`#0F172A`, `#24292E`, GitHub green `#2EA44F`, and studio crimson accents).
  - Material 3 cards with distinct visual status badges (`LINKED`, `VALID TOKEN`, `SIMULATION MODE`).

- **Interactive Feedback & Motion**:
  - Instant Toast feedback when token is pasted or saved.
  - Animated `LinearProgressIndicator` during token validation and remote commit.
  - Success banner with Commit SHA and direct link to the newly created commit on GitHub.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Direct Clipboard Paste Helper**
  - *Chosen Approach*: Add `IconButton` using `LocalClipboardManager` right inside the `trailingIcon` of the PAT TextField.
  - *Why*: Typing a 40-character hexadecimal token on mobile keyboards is error-prone. One tap ensures 100% accuracy.

- **Decision 2: 1-Click "Generate Token" Shortcut**
  - *Chosen Approach*: Link directly to `https://github.com/settings/tokens/new?scopes=repo&description=CreatorStudioSync`.
  - *Why*: Guides users directly to GitHub's token creation form with required scopes pre-checked.

- **Decision 3: Room DB Credential Persistence**
  - *Chosen Approach*: Update `settings.gitHubToken` in `CreatorSettingsDao`.
  - *Why*: Keeps credentials persistent across sessions and device restarts so automatic 10-minute sync jobs can push without prompting.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                      GitHubLinkScreen                       │
│  ┌───────────────────────┐       ┌───────────────────────┐  │
│  │  Username / Repo / Br │       │  PAT Input with Paste │  │
│  │  punithjr2            │       │  & Visibility Toggle  │  │
│  └───────────┬───────────┘       └───────────┬───────────┘  │
│              │                               │              │
│              ▼                               ▼              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │       CreatorStudioViewModel / Settings Repository    │  │
│  │       (Saves gitHubToken to CreatorSettingsEntity)    │  │
│  └───────────────────────────┬───────────────────────────┘  │
└──────────────────────────────┼──────────────────────────────┘
                               ▼
        ┌──────────────────────────────────────────────┐
        │               GitHubRepository               │
        │  ┌────────────────────────────────────────┐  │
        │  │ fetchUserProfile(username, token)      │  │
        │  ├────────────────────────────────────────┤  │
        │  │ syncProjectToRepo(...) -> Live Commit  │  │
        │  └────────────────────────────────────────┘  │
        └──────────────────────┬───────────────────────┘
                               ▼
                ┌──────────────────────────────┐
                │        GitHub REST API       │
                │  api.github.com/repos/...    │
                └──────────────────────────────┘
```

- **Entities & State**:
  - `CreatorSettingsEntity.gitHubToken`: Stores the PAT securely in SQLite via Room.
  - `GitHubLinkScreen.tokenInput`: Local reactive state with show/hide password toggle.
  - `GitHubSyncResult`: Encapsulates sync success status, commit SHA, and remote URL.
