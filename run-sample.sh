#!/bin/bash
# Helper script to run JavaSteam samples inside Docker container

set -e

if [ -z "$1" ]; then
    echo "Usage: ./run-sample.sh <sample_number> [username] [password]"
    echo "Example: ./run-sample.sh 001 myuser mypass"
    echo ""
    echo "Available samples:"
    echo "  000 - Authentication"
    echo "  001 - Authentication with QR Code"
    echo "  015 - Achievements"
    echo "  020 - Friends"
    echo ""
    echo "If username/password not provided, will use STEAM_USER and STEAM_PASS env vars"
    echo "Note: You will be prompted for 2FA code interactively after login attempt"
    exit 1
fi

SAMPLE_NUM=$1
USERNAME=$2
PASSWORD=$3

if [ -z "$USERNAME" ] || [ -z "$PASSWORD" ]; then
    echo "Error: Steam credentials not provided"
    echo "Either pass them as arguments or set STEAM_USER and STEAM_PASS environment variables"
    exit 1
fi

# Map sample numbers to class names
case $SAMPLE_NUM in
    "000")
        SAMPLE_CLASS="in.dragonbra.javasteamsamples._000_authentication.SampleLogonAuthentication"
        ;;
    "001")
        SAMPLE_CLASS="in.dragonbra.javasteamsamples._001_authenticationwithqrcode.SampleLogonAuthenticationQRCode"
        ;;
    "015")
        SAMPLE_CLASS="in.dragonbra.javasteamsamples._015_achievements.SampleAchievements"
        ;;
    "020")
        SAMPLE_CLASS="in.dragonbra.javasteamsamples._020_friends.SampleFriends"
        ;;
    *)
        echo "Unknown sample number: $SAMPLE_NUM"
        exit 1
        ;;
esac

echo "Building project..."
./gradlew :javasteam-samples:classes --no-daemon

echo ""
echo "Running sample $SAMPLE_NUM: $SAMPLE_CLASS"
echo "Username: $USERNAME"
echo "========================================"
echo ""

# Run the sample with interactive stdin for 2FA codes
./gradlew :javasteam-samples:run \
    -PmainClass="$SAMPLE_CLASS" \
    --args="$USERNAME $PASSWORD" \
    --console=plain \
    --no-daemon
