The script is intentionally damaged through setting the
schema version to 0, while it's 1 for BB version 2.23.
This will bring SQL error during migration, so we could
test database shutdown and resetting.