
var sortedVariables = java.util.TreeMap(java.lang.System.getenv());
var keysIterator = sortedVariables.keySet().iterator();

while(keysIterator.hasNext()){
  var key = keysIterator.next();
  echo(key + " = "+sortedVariables.get(key));
}
