#!/bin/bash

# --- Helper Functions (omitted for brevity) ---

fail() {
    printf "%b\n\n" "${RED}${1}${CLEAR}"
    printHelp
    exit 1
}

failNoHelp() {
    printf "%b\n" "${RED}${1}${CLEAR}"
    exit 1
}

printArgHelp() {
    if [ -z "${1}" ]; then
        printf "%b%s    %-20s%b%s\n" "${YELLOW}" "" "${2}" "${CLEAR}" "${3}"
    else
        printf "%b%s, %-20s%b%s\n" "${YELLOW}" "${1}" "${2}" "${CLEAR}" "${3}"
    fi
}

printHelp() {
    echo "Performs a release of the project. The release argument and value and the development argument and value are required parameters."
    echo "Any additional arguments are passed to the Maven command."
    echo ""
    printArgHelp "-d" "--development" "The next version for the development cycle."
    printArgHelp "-f" "--force" "Forces to allow a SNAPSHOT suffix in release version and not require one for the development version."
    printArgHelp "-h" "--help" "Displays this help."
    printArgHelp "" "--notes-start-tag" "When doing a GitHub release, indicates the tag to use as the starting point for generating release notes."
    printArgHelp "-r" "--release" "The version to be released. Also used for the tag."
    printArgHelp "" "--dry-run" "Executes the release in as a dry-run. Nothing will be updated or pushed."
    printArgHelp "-v" "--verbose" "Prints verbose output."
    echo ""
    echo "Usage: ${0##*/} --release 1.0.0 --development 1.0.1-SNAPSHOT"
}

# --- Initialization & Color Setup (omitted for brevity) ---

CLEAR=""
RED=""
YELLOW=""

if [[ -t 1 ]] && [[ -z "${NO_COLOR-}" ]] && [ "$(tput colors)" -ge 8 ]; then
    CLEAR="\033[0m"
    RED="\033[0;31m"
    YELLOW="\033[0;33m"
fi

DRY_RUN=false
FORCE=false
DEVEL_VERSION=""
RELEASE_VERSION=""
SCRIPT_PATH=$(realpath "${0}")
SCRIPT_DIR=$(dirname "${SCRIPT_PATH}")
LOCAL_REPO="/tmp/m2/repository/$(basename "${SCRIPT_DIR}")"
VERBOSE=""

CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null) || {
    echo "Error: This script must be run from within a git repository"
    exit 1
}

MAVEN_ARGS=()

if [ -z "${DAYS}" ]; then
    DAYS="5"
fi

# --- Argument Parsing (omitted for brevity) ---

while [ "$#" -gt 0 ]
do
    case "${1}" in
        -d|--development)
            if [ -z "${2:-}" ] || [[ "${2}" =~ ^- ]]; then
                fail "The --development flag requires a value"
            fi
            DEVEL_VERSION="${2}"
            shift
            ;;
        --dry-run)
            DRY_RUN=true
            ;;
        -f|--force)
            FORCE=true
            ;;
        -h|--help)
            printHelp
            exit 0
            ;;
        -r|--release)
            if [ -z "${2:-}" ] || [[ "${2}" =~ ^- ]]; then
                fail "The --release flag requires a value"
            fi
            RELEASE_VERSION="${2}"
            shift
            ;;
        -v|--verbose)
            VERBOSE="-v"
            ;;
        *)
            MAVEN_ARGS+=("${1}")
            ;;
    esac
    shift
done

# --- Validation (omitted for brevity) ---

if [ -z "${DEVEL_VERSION}" ]; then
    fail "The development version is required."
fi

if [ -z "${RELEASE_VERSION}" ]; then
    fail "The release version is required."
fi

if ! ${FORCE}; then
    if  [[ "${RELEASE_VERSION}" =~ "SNAPSHOT" ]]; then
        failNoHelp "The release version appears to be a SNAPSHOT (${RELEASE_VERSION}). This is likely not valid and -f should be used if it is."
    fi
    if  [[ ! "${DEVEL_VERSION}" =~ "SNAPSHOT" ]]; then
        failNoHelp "The development version does not appear to be a SNAPSHOT (${DEVEL_VERSION}). This is likely not valid and -f should be used if it is."
    fi
fi

# Check the settings to ensure a server defined with that value
SERVER_ID=$(mvn help:evaluate -Dexpression=nexus.serverId -B -q -DforceStdout "${MAVEN_ARGS[@]}" 2>/dev/null | sed 's/^\[INFO\] \[stdout\] //')
if ! mvn help:effective-settings | grep -q "<id>${SERVER_ID}</id>"; then
    failNoHelp "A server with the id of \"${SERVER_ID}\" was not found in your settings.xml file."
fi

printf "Performing release for version %s with the next version of %s\n" "${RELEASE_VERSION}" "${DEVEL_VERSION}"

TAG_NAME="v${RELEASE_VERSION}"

if ${DRY_RUN}; then
    echo "This will be a dry run and nothing will be updated or pushed."
    MAVEN_ARGS+=("-DdryRun" "-DpushChanges=false")
fi

# --- Local Repository Cleanup (omitted for brevity) ---

if [ -d "${LOCAL_REPO}" ]; then
    echo "Cleaning up local Maven repository at ${LOCAL_REPO}..."
    find "${LOCAL_REPO}" -type d -mtime +"${DAYS}" -print0 | xargs -0 -I {} rm -rf ${VERBOSE} "{}"
    find "${LOCAL_REPO}" -type d -name "*SNAPSHOT" -print0 | xargs -0 -I {} rm -rf ${VERBOSE} "{}"
    PROJECT_PATH="$(mvn help:evaluate -Dexpression=project.groupId -B -q -DforceStdout "${MAVEN_ARGS[@]}" 2>/dev/null | sed 's/^\[INFO\] \[stdout\] //')"
    PROJECT_PATH="${LOCAL_REPO}/${PROJECT_PATH//./\/}"
    rm -rf ${VERBOSE} "${PROJECT_PATH}"
fi

# --- Maven Command Execution (omitted for brevity) ---

maven_command=(
    mvn clean release:clean release:prepare release:perform
    "-Dmaven.repo.local=${LOCAL_REPO}"
    "-DdevelopmentVersion=${DEVEL_VERSION}"
    "-DreleaseVersion=${RELEASE_VERSION}"
    "-Dtag=${TAG_NAME}"
    "${MAVEN_ARGS[@]}"
)

if [ "-v" = "${VERBOSE}" ]; then
    printf "\n\nExecuting:\n  %s\n" "${maven_command[*]}"
fi

"${maven_command[@]}"
status=$?

# --- Post-Execution ---

if [ ${status} -eq 0 ]; then
    echo ""
    echo "Your release has been successful. Check the validation after $(date -d "now + 10 minutes" +"%H:%M:%S") on https://repository.jboss.org/nexus."
    echo ""
    echo "Once validation is successful, execute the following commands:"
    echo ""

    # Instructions for Git and Staging Move (Always printed)
    cat <<EOF
git checkout ${TAG_NAME}
mvn nxrm3:staging-move -Dmaven.repo.local="${LOCAL_REPO}"
git checkout ${CURRENT_BRANCH}
git push upstream ${CURRENT_BRANCH}
git push upstream ${TAG_NAME}
EOF

    # Conditional Instructions for GitHub CLI (Only printed if GH is available)
    if command -v gh &>/dev/null; then
        echo ""
        echo "Next step: GitHub Release Creation"
        echo "-----------------------------------"
        result="$(gh repo set-default --view 2>&1)"
        if [[ "${result}" =~ "gh repo set-default" ]]; then
            # Not configured
            printf "%b\n" "${RED}NOTE: The default repository has not been set. You must run 'gh repo set-default' first.${CLEAR}"
            echo "Then execute: gh release create --generate-notes --latest --verify-tag ${TAG_NAME}"
        else
            if ! ${DRY_RUN}; then
                # Actual command to be executed
                echo "Execute this command:"
                echo "gh release create --generate-notes --latest --verify-tag ${TAG_NAME}"
            fi
        fi
    else
        echo ""
        echo "The 'gh' commands are not available. You must manually create a release for the GitHub tag ${TAG_NAME}."
    fi

else
    printf "\nThe release has failed. See the previous errors and try again. The command executed was:\n%s\n" "${maven_command[*]}"
fi
exit ${status}