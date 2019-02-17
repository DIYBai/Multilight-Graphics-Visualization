To run, simply compile with "javac *.java" then run the Draw program with the
command "java Draw".

For parallel version, use the command "parallel.bat" which will compile then run
the program automatically. Look in the 2-line bat file to see the specific commands
for compiling vs running the parallel versions. The parallel version does not
have any dependencies so should work out of the gate, however it is possible that
if the JRE is used and not the JDK, a fatal error will occur involving the lack
of a "tools.jar" file in the JRE lib. Simply copying that file from the JDK to the
JRE was sufficient to solve this for me.

The program allows the addition of more light sources of various hues, radii,
and positions. It also allows clearing the scene of all lights as well as rotations
of the two cubes I have in the scene.
