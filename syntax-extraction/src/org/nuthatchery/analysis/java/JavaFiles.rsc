module org::nuthatchery::analysis::java::JavaFiles
import IO;
import org::nuthatchery::analysis::java::Java8;
import ParseTree;
import List;

list[loc] javaFiles = [
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/util/IGenerator.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/util/generators/AbstractGenerator.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/util/generators/GridDirectionGenerator.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/util/generators/DoubleGenerator.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/util/generators/ListGenerator.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/util/generators/LocationGenerator.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/util/generators/IntGenerator.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/util/generators/GridGenerator.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/util/generators/AreaGenerator.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/util/generators/ElementGenerator.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/util/generators/StringGenerator.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/events/IEvent.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/events/GameEvent.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/examples/Rabbit.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/examples/Carrot.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/examples/ExampleItem.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/game/Game.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/game/IllegalMoveException.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/game/IGame.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/tests/PlayerTest.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/tests/GameMapTest.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/Main.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/AppInfo.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/map/IMapView.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/map/GameMap.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/map/MapReader.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/map/IGameMap.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/objects/IActor.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/objects/INonPlayer.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/objects/IItem.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/objects/Wall.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/objects/Dust.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/rogue101/objects/IPlayer.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/Screen.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/IPaintLayer.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/textmode/TextMode.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/textmode/Printer.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/textmode/TextFontAdjuster.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/textmode/ControlSequences.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/textmode/BlocksAndBoxes.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/textmode/DemoPages.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/textmode/TextFont.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/gfxmode/ShapePainter.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/gfxmode/IPainter.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/gfxmode/Gravity.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/gfxmode/TurtlePainter.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/gfxmode/Point.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/gfxmode/ITurtle.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/gfxmode/Direction.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/gfx/gfxmode/IShape.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/grid/IArea.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/grid/MultiGrid.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/grid/IGrid.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/grid/IMultiGrid.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/grid/RectArea.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/grid/IPosition.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/grid/ILocation.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/grid/MyGrid.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/grid/tests/AreaRetting.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/grid/tests/GridRetting.java|,
|file:///home/anya/inf101/git/inf101.v18.sem1/src/inf101/v18/grid/GridDirection.java|];

public list[loc] findJavaFiles(loc dir) {
	list[loc] result = [];
	for(f <- listEntries(dir)) {
		if(isDirectory(dir + f))
			result += findJavaFiles(dir + f);
		else if(/.*\.java$/ := f)
			result += dir + f;
	}
	return result;
}
public void testAll(loc dir) {
	ok = 0;
	files = 0;
	for(l <- findJavaFiles(dir)) {
		files = files + 1;
		try {
			t = parse(#start[CompilationUnit], l, allowAmbiguity = true);
			//println("OK  <l.path>");
			ok = ok + 1;
		}
		catch ParseError(errLoc): {
			println("ERR  <l.path>:");
			//println("  <errLoc>");
			//println("  <size(readFileLines(l))>");
			println("  <readFileLines(l)[errLoc.begin.line-1]>");
		}
	}
	println("<files> files, <ok> ok, <files-ok> bad");
}