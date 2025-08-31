#!/bin/bash

# Allure Folder Report Customizer Script
# Generates a default Allure folder report and injects custom CSS, JS, favicon, and title

set -e

# Configuration
ALLURE_RESULTS_DIR="allure-results"
ALLURE_REPORT_DIR="allure-report"
PROPERTIES_PATH="src/test/resources/allure.properties"
CSS_PATH="src/test/resources/custom-logo-plugin/style.css"
JS_PATH="src/test/resources/custom-logo-plugin/index.js"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status()   { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success()  { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_warning()  { echo -e "${YELLOW}[WARNING]${NC} $1"; }
print_error()    { echo -e "${RED}[ERROR]${NC} $1"; }

check_prerequisites() {
    print_status "Checking prerequisites..."
    if [ ! -d "$ALLURE_RESULTS_DIR" ]; then
        print_error "Directory $ALLURE_RESULTS_DIR not found!"
        exit 1
    fi
    if ! command -v allure &> /dev/null; then
        print_error "Allure command not found. Please install Allure."
        exit 1
    fi
    print_success "All prerequisites met"
}

get_property() {
    grep "^$1=" "$PROPERTIES_PATH" | cut -d'=' -f2-
}

main() {
    print_status "Starting Allure Folder Report Customization..."
    echo "================================================"

    check_prerequisites

    # Step 1: Generate Allure folder report
    print_status "Generating Allure folder report..."
    allure generate "$ALLURE_RESULTS_DIR" --clean -o "$ALLURE_REPORT_DIR"
    print_success "Allure report generated at $ALLURE_REPORT_DIR"

    REPORT_HTML="$ALLURE_REPORT_DIR/index.html"
    if [ ! -f "$REPORT_HTML" ]; then
        print_error "index.html not found in $ALLURE_REPORT_DIR"
        exit 1
    fi

    # Step 2: Read custom assets
    print_status "Reading custom CSS and JS assets..."
    if [ ! -f "$CSS_PATH" ]; then
        print_warning "Custom CSS file not found at $CSS_PATH"
        CUSTOM_CSS=""
    else
        CUSTOM_CSS=$(cat "$CSS_PATH")
        print_success "Custom CSS loaded"
    fi

    if [ ! -f "$JS_PATH" ]; then
        print_warning "Custom JS file not found at $JS_PATH"
        CUSTOM_JS=""
    else
        CUSTOM_JS=$(cat "$JS_PATH")
        print_success "Custom JS loaded"
    fi

    CUSTOM_TITLE=$(get_property "allure.report.title")
    CUSTOM_FAVICON=$(get_property "allure.report.favicon")

    # Step 3: Inject customizations (using awk/perl for robustness)
    print_status "Injecting customizations..."

    # Title
    if [ -n "$CUSTOM_TITLE" ]; then
        perl -pi -e "s|<title>.*?</title>|<title>$CUSTOM_TITLE</title>|g" "$REPORT_HTML"
        print_success "Custom title injected"
    else
        print_warning "No custom title found in $PROPERTIES_PATH"
    fi

    # Favicon
    if [ -n "$CUSTOM_FAVICON" ]; then
        perl -pi -e 's|<link[^>]*rel=["'\'']icon["'\''][^>]*>||g' "$REPORT_HTML"
        perl -pi -e "s|</head>|<link rel=\"icon\" href=\"$CUSTOM_FAVICON\"/>\n</head>|g" "$REPORT_HTML"
        print_success "Custom favicon injected"
    else
        print_warning "No custom favicon found in $PROPERTIES_PATH"
    fi

    # CSS (awk for multiline)
    if [ -n "$CUSTOM_CSS" ]; then
        awk -v css="$CUSTOM_CSS" '
            /<\/head>/ && !done_css {print "<style>\n" css "\n</style>"; done_css=1}
            {print}
        ' "$REPORT_HTML" > "$REPORT_HTML.tmp" && mv "$REPORT_HTML.tmp" "$REPORT_HTML"
        print_success "Custom CSS injected"
    else
        print_warning "No custom CSS injected"
    fi

    # JS (awk for multiline)
    if [ -n "$CUSTOM_JS" ]; then
        awk -v js="$CUSTOM_JS" '
            /<\/body>/ && !done_js {print "<script>\n" js "\n</script>"; done_js=1}
            {print}
        ' "$REPORT_HTML" > "$REPORT_HTML.tmp" && mv "$REPORT_HTML.tmp" "$REPORT_HTML"
        print_success "Custom JS injected"
    else
        print_warning "No custom JS injected"
    fi

    print_success "Customizations applied!"

    # Step 4: Open report
    print_status "Opening customized Allure report..."
    allure open "$ALLURE_REPORT_DIR"

    echo "================================================"
    print_success "Customized Allure folder report is ready!"
    print_status "Features included:"
    echo "  - Custom logo, animation, and footer via CSS/JS"
    echo "  - Dynamic title and favicon from allure.properties"
    echo "  - Default Allure folder report structure"
}

main "$@"