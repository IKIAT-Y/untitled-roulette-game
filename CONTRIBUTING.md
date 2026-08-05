# Contributing

Thank you for your interest in contributing!

## Development Workflow

This project uses a **fork-and-pull** workflow.

### 1. Fork the repository

Click **Fork** on GitHub to create your own copy.

### 2. Clone your fork

```bash
git clone https://github.com/<your-username>/<repository>.git
cd <repository>
```

### 3. Add the upstream repository

```bash
git remote add upstream https://github.com/Yorifuji-T/untitled-roulette-game.git
```

Verify:

```bash
git remote -v
```

You should see:

```bash
origin  https://github.com/<your-username>/<repository>.git (fetch)
origin  https://github.com/<your-username>/<repository>.git (push)
upstream        https://github.com/Yorifuji-T/untitled-roulette-game.git (fetch)
upstream        https://github.com/Yorifuji-T/untitled-roulette-game.git (push)
```

### 4. Create a feature branch

Never work directly on `main`.

```bash
git checkout -b feat/username/feature-to-add
```

e.g.

````bash
git checkout -b feat/yrska/bouncy-charm-upgrade
```

For bug fixes:

```bash
git checkout -b fix/user/issue-description
```

```bash
git checkout -b fix/OveralZ/ball-delta-time
```

### 5. Make your changes

Before committing, ensure:

```bash
./gradlew test
./gradlew check
```

### 6. Commit your changes

Write clear commit messages.
Use conventional prefixes e.g. `feat:`, `fix:`, `chore:`

Example:

```
feat: Add highscore tracking

fix: Fix crash when loading save files

chore: Improve README documentation
```

### 7. Keep your fork updated

```bash
git fetch upstream
git checkout main
git merge upstream/main
git push origin main
```

### 8. Push your branch

```bash
git push origin feature/my-new-feature
```

### 9. Open a Pull Request

Open a PR from your fork into the upstream `main` branch.

Please include:

- A clear description
- Screenshots if applicable
- Any related issue numbers

## Code Style

- Follow existing formatting. For more information see [Style Guide](https://github.com/Yorifuji-T/untitled-roulette-game/wiki/Style-and-Organization-Guide)
- Write meaningful variable and method names.
- Keep methods focused on a single responsibility.
- Add comments only where they improve understanding.

## Testing

All pull requests should:

- Pass all tests
- Build successfully
- Not introduce compiler warnings where possible

## Reporting Bugs

When opening an issue, include:

- Steps to reproduce
- Expected behaviour
- Actual behaviour
- Java version
- Operating system
- Relevant logs
````
