Changing release number:

RGENERATE APPLICATION FOR TESTING COMPILE:
- Make sure to run the unit tests once, to be sure to have the qooxdoo-sdk downloded into:
target/test-compile/qooxdoo-sdk
- Change the compile-app qx application by creating a new gui application with the 'create-application.py' script, using the right sdk:
python ../../../target/test-compile/qooxdoo-sdk/tool/bin/create-application.py -n compile_app

