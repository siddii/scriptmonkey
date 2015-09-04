/*
    Simple script to prove that the script monkey can work with external libraries.
    Make sure that ant.jar is included in the project libraries before you
    running this script from.
*/


var antPkgs = new JavaImporter(org.apache.tools.ant);

with (antPkgs) {

    function showAntVersion() {
        echo(Main.getAntVersion());
    }

}
var tools = showAntVersion();