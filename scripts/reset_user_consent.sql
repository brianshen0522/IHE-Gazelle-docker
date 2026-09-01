-- This script resets the user_consent table to false for all users.
UPDATE gum.user_consent SET consent=false;