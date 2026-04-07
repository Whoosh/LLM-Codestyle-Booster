package test;
import java.util.List;
class MapperTest {

void test() { Mapper mapper = new Mapper(); List.of("a").stream().map(mapper::transform); }
}
