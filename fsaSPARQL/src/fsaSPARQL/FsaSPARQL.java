package fsaSPARQL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Scanner;
import org.apache.log4j.varia.NullAppender;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.expr.ExprFunctionOp;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementAssign;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementDataset;
import com.hp.hpl.jena.sparql.syntax.ElementExists;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;
import com.hp.hpl.jena.util.FileUtils;

public class FsaSPARQL {

	private static String readFile(String pathname) throws IOException {

		File file = new File(pathname);
		StringBuilder fileContents = new StringBuilder((int) file.length());
		Scanner scanner = new Scanner(file);
		String lineSeparator = System.getProperty("line.separator");

		try {
			while (scanner.hasNextLine()) {
				fileContents.append(scanner.nextLine() + lineSeparator);
			}
			return fileContents.toString();
		} finally {
			scanner.close();
		}
	}

	public static String FSAtoSPARQL(String queryString) {
		final Query query = QueryFactory.create(queryString);

		ElementWalker.walk(query.getQueryPattern(), new ElementVisitorBase() {

			@Override
			public void visit(ElementPathBlock el) {

				ListIterator<TriplePath> it = el.getPattern().iterator();
				while (it.hasNext()) {
					Item(it);
				}
			}

			@Override
			public void visit(ElementSubQuery el) {
				ElementGroup subQP = (ElementGroup) el.getQuery().getQueryPattern();
				ElementWalker.walk(subQP, this);
			}
			
			@Override
			public void visit(ElementGroup group) {
				 
					super.visit(group);
				 
			}

			@Override
			public void visit(ElementAssign assign) {
				super.visit(assign);
			}
			
			@Override
			public void visit(ElementData data) {
				super.visit(data);
			}
			
			@Override
			public void visit(ElementExists exists) {
				super.visit(exists);
			}
			
			@Override
			public void visit(ElementMinus minus) {
				super.visit(minus);
			}
			
			@Override
			public void visit(ElementNamedGraph namedGraph) {
				super.visit(namedGraph);
			}
			
			@Override
			public void visit(ElementNotExists notExists) {
				super.visit(notExists);
			}
			
			@Override
			public void visit(ElementOptional optional) {
				super.visit(optional);
			}
			
			 
			@Override
			public void visit(ElementService service) {
				super.visit(service);
			}
			
			public void visit(ElementTriplesBlock el) {
				super.visit(el);
			}
			
			@Override
			public void visit(ElementUnion arqUnion) {
				super.visit(arqUnion);
			}
			
			@Override
			public void visit(ElementFilter el) {

				if (el.getExpr().getFunction().getFunctionName(null) == "exists") {
					ElementWalker.walk(((ExprFunctionOp) el.getExpr().getFunction()).getElement(), this);
				}
				if (el.getExpr().getFunction().getFunctionName(null) == "notexists") {
					ElementWalker.walk(((ExprFunctionOp) el.getExpr().getFunction()).getElement(), this);
				}
			}
			 

		});
		return query.toString();
	};

	public static void Item(ListIterator<TriplePath> it) {
		TriplePath tp = it.next();

		if (tp.getObject().isVariable() && it.hasNext()) {
			TriplePath item = it.next(); // first

			if (item.getPredicate().hasURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#first")) {
				it.remove();// remove first
				if (tp.getPredicate().hasURI("http://www.fuzzy.org#type")) {

					it.add(new TriplePath(new Triple(item.getSubject(),
							NodeFactory.createURI("http://www.fuzzy.org#onProperty"), item.getObject())));
					it.next();
					it.remove();
					TriplePath cl = it.next();
					it.remove();
					it.add(new TriplePath(new Triple(item.getSubject(),
							NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), cl.getObject())));
				} else if (tp.getPredicate().hasURI("http://www.fuzzy.org#subPropertyOf")) {
					it.add(new TriplePath(new Triple(item.getSubject(),
							NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#subPropertyOf"),
							item.getObject())));
				} else if (tp.getPredicate().hasURI("http://www.fuzzy.org#subClassOf")) {
					it.add(new TriplePath(new Triple(item.getSubject(),
							NodeFactory.createURI("http://www.w3.org/2000/01/rdf-schema#subClassOf"),
							item.getObject())));
				} else
					it.add(new TriplePath(new Triple(item.getSubject(),
							NodeFactory.createURI("http://www.fuzzy.org#item"), item.getObject())));
				it.next(); // rest
				it.remove();// remove rest
				TriplePath truth = it.next();// first
				it.remove();// remove first
				it.add(new TriplePath(new Triple(item.getSubject(), NodeFactory.createURI("http://www.fuzzy.org#truth"),
						truth.getObject())));
				it.next();// rest
				it.remove();// remove rest

			} else {
				it.previous();
			}
		}
	};

	public static String fsaSPARQL(String model, String query) {

		String s1 = FSAtoSPARQL(query);

		FunctionRegistry.get().put("http://www.fuzzy.org#AT_LEAST", AT_LEAST.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#AT_MOST", AT_MOST.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#CLOSE_TO", CLOSE_TO.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#MORE_OR_LESS", MORE_OR_LESS.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#VERY", VERY.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#AND_PROD", AND_PROD.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#OR_PROD", OR_PROD.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#AND_LUK", AND_LUK.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#OR_LUK", OR_LUK.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#AND_GOD", AND_GOD.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#OR_GOD", OR_GOD.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#MIN", MIN.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#MAX", MAX.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#MEAN", MEAN.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#WSUM", WSUM.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#WMAX", WMAX.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#WMIN", WMIN.class);

		String s2 = SPARQL(model, s1);
		return s2;
	}

	public static String SPARQL(String filei, String queryStr) {

		OntModel model = ModelFactory.createOntologyModel();
		model.read(filei);
		com.hp.hpl.jena.query.Query query = QueryFactory.create(queryStr);

		if (query.isSelectType()) {
			ResultSet result = (ResultSet) QueryExecutionFactory.create(query, model).execSelect();
			File theDir = new File("tmp-sparql");
			if (!theDir.exists()) {
				theDir.mkdir();
			}
			String fileName = "./tmp-sparql/" + "result.owl";
			File f = new File(fileName);
			FileOutputStream file;
			try {
				file = new FileOutputStream(f);
				ResultSetFormatter.outputAsXML(file, (com.hp.hpl.jena.query.ResultSet) result);
				try {
					file.close();

				} catch (IOException e) {

					e.printStackTrace();
				}
			} catch (FileNotFoundException e1) {

				e1.printStackTrace();
			}

			String s = "";
			try {
				s = readFile(fileName);
			} catch (IOException e) {

				e.printStackTrace();
			}

			final File[] files = theDir.listFiles();
			for (File g : files)
				g.delete();
			theDir.delete();
			return s;
		} else

		if (query.isConstructType()) {
			Model result = QueryExecutionFactory.create(query, model).execConstruct();
			File theDir = new File("tmp-sparql");
			if (!theDir.exists()) {
				theDir.mkdir();
			}
			String fileName = "./tmp-sparql/" + "result.owl";
			File f = new File(fileName);
			FileOutputStream file;
			try {
				file = new FileOutputStream(f);
				result.write(file, FileUtils.langXMLAbbrev);
				try {
					file.close();

				} catch (IOException e) {

					e.printStackTrace();
				}
			} catch (FileNotFoundException e1) {

				e1.printStackTrace();
			}

			String s = "";
			try {
				s = readFile(fileName);
			} catch (IOException e) {

				e.printStackTrace();
			}
			final File[] files = theDir.listFiles();
			for (File g : files)
				g.delete();
			theDir.delete();
			return s;
		} else

		if (query.isDescribeType()) {
			Model result = QueryExecutionFactory.create(query, model).execDescribe();

			File theDir = new File("tmp-sparql");
			if (!theDir.exists()) {
				theDir.mkdir();
			}
			String fileName = "./tmp-sparql/" + "result.owl";

			File f = new File(fileName);

			FileOutputStream file;

			try {
				file = new FileOutputStream(f);
				result.write(file, FileUtils.langXMLAbbrev);
				try {
					file.close();

				} catch (IOException e) {

					e.printStackTrace();
				}
			} catch (FileNotFoundException e1) {

				e1.printStackTrace();
			}

			String s = "";
			try {
				s = readFile(fileName);
			} catch (IOException e) {

				e.printStackTrace();
			}
			final File[] files = theDir.listFiles();
			for (File g : files)
				g.delete();
			theDir.delete();
			return s;
		} else

		{
			Boolean b = QueryExecutionFactory.create(query, model).execAsk();
			return b.toString();
		}

	};

	public static void main(String[] args) {

		String movies = "movies.rdf";
		String hotels = "hotels.rdf";
		String hashtagm = "Hashtag.rdf";
		String UserShowm = "UserShow.rdf";
		String User_Time_Linem = "User_Time_Line.rdf";
		String Retweetersm = "Retweeters.rdf";
		String UserIDm = "UserID.rdf";
		String User_Friendsm = "User_Friends.rdf";
		String User_Tipsm = "User_Tips.rdf";
		String VenueIDm = "VenueID.rdf";
		String Venue_Tipsm = "Venue_Tips.rdf";
		String Venue_SimilarVenuesm = "Venue_SimilarVenues.rdf";
		String Nearm = "Near.rdf";
		String MovieIDm = "MovieID.rdf";
		String Creditsm = "Credits.rdf";
		String Reviewsm = "Movie_Reviews.rdf";
		String Now_Playingm = "Now_Playing.rdf";
		String PersonIDm = "PersonID.rdf";
		String Searchm = "Search.rdf";

		String fuzzy1 = "PREFIX movie: <http://www.movies.org#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org#>"
				+ "SELECT ?Name ?Rank " + "WHERE { ?Movie movie:name ?Name ."
				+ " ?Movie movie:leading_role (?Actor ?l) ." + " ?Actor movie:name 'George Clooney' ."
				+ " ?Movie f:type (movie:genre movie:Thriller ?t) ." + " BIND (f:AND_PROD(?t,?l) as ?Rank) . "
				+ " FILTER (?Rank > 0.5) }";

		String fuzzy2 = "PREFIX movie: <http://www.movies.org#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org#>"
				+ "SELECT ?Name ?Rank " + "where { ?Movie movie:name ?Name ."
				+ "?Movie movie:leading_role (?Actor ?l) ." + "?Actor movie:name 'George Clooney' . "
				+ "{ ?Movie f:type (movie:genre movie:Thriller ?c) } "
				+ "union { ?Movie f:type (movie:genre movie:Comedy ?c) } ."
				+ "?Movie f:type (movie:quality movie:Good ?r) ." + "BIND(f:AND_PROD(f:AND_PROD(?r,?l),?c) as ?Rank)}"
				+ "ORDERBY DESC(?Rank) ";

		String fuzzy3 = "PREFIX movie: <http://www.movies.org#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org#>"
				+ "SELECT ?Name  ?Rank " + "WHERE { ?Movie movie:name ?Name ."
				+ " ?Movie f:type (movie:quality movie:Excellent ?l) ."
				+ " ?Movie f:type (movie:genre movie:Thriller ?t) ." + " BIND(f:OR_PROD(?t,?l) as ?Rank) ."
				+ " FILTER (?Rank > 0.4) }";

		String fuzzy4 = "PREFIX hotel: <http://www.hotels.org#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org#>"
				+ "SELECT ?Name ?pi ?l " + "WHERE { ?Hotel hotel:name ?Name ." + "?Hotel rdf:type hotel:Hotel ."
				+ "?Hotel hotel:close (?pi ?l) ." + "?pi hotel:name 'Empire State Building'." + " FILTER (?l > 0.25) }";

		String fuzzy5 = "PREFIX hotel: <http://www.hotels.org#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org#>"
				+ "SELECT ?Name ?p ?d " + "WHERE { ?Hotel hotel:name ?Name ." + "?Hotel rdf:type hotel:Hotel ."
				+ "?Hotel hotel:price ?p ." + "BIND(f:AT_MOST(?p,200,300) as ?d) }";

		String fuzzy6 = "PREFIX hotel: <http://www.hotels.org#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org#>"
				+ "SELECT ?Name ?p ?d " + "WHERE { ?Hotel hotel:name ?Name ." + "?Hotel rdf:type hotel:Hotel ."
				+ "?Hotel hotel:price ?p ." + "?Hotel f:type (hotel:quality hotel:Good ?g) ."
				+ "?Hotel f:type (hotel:style hotel:Elegant ?e) . " + "BIND(f:WSUM(0.1,f:AND_PROD(f:MORE_OR_LESS(?e),"
				+ "f:VERY(?g)),0.9,f:CLOSE_TO(?p,100,50)) as ?d) }";

		String fuzzy7 = "PREFIX hotel: <http://www.hotels.org#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org#>"
				+ "SELECT ?Hotel ?g " + "WHERE " + "{ " + "?Hotel rdf:type hotel:Hotel " + "{ "
				+ "SELECT ?Name WHERE { ?Hotel f:type (hotel:quality hotel:Good ?g) } }" + "}";

		String fuzzy8 = "PREFIX hotel: <http://www.hotels.org#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org#>"
				+ "SELECT ?Hotel ?g " + "WHERE " + "{ " + "?Hotel rdf:type hotel:Hotel "
				+ "OPTIONAL { ?Hotel f:type (hotel:quality hotel:Good ?g) } " + "}";

		String fuzzy9 = "PREFIX hotel: <http://www.hotels.org#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org#>"
				+ "SELECT ?Hotel ?g " + "WHERE " + "{ {" + "?Hotel rdf:type hotel:Hotel " + "}"
				+ "MINUS { ?Hotel f:type (hotel:quality hotel:Good ?g) } " + "}";

		String fuzzy10 = "PREFIX hotel: <http://www.hotels.org#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org#>"
				+ "SELECT * WHERE\r\n" + "{" + "{" + "    SELECT ?Hotel WHERE" + "    {"
				+ "         ?Hotel f:type (hotel:quality hotel:Good ?l)" + "    }" + "}" + "UNION" + "{"
				+ "    SELECT ?Hotel WHERE" + "    {" + "         ?Hotel f:type (hotel:quality hotel:Good ?l)" + "    }"
				+ "}" + "}";

		String fuzzy11 = "PREFIX hotel: <http://www.hotels.org#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org#>"
				+ "SELECT ?Hotel ?g " + "WHERE " + "{ " + "?Hotel rdf:type hotel:Hotel " + "{ "
				+ "FILTER EXISTS {SELECT ?Name WHERE { ?Hotel f:type (hotel:quality hotel:Good ?g) } }}" + "}";

		String fuzzy12 = "PREFIX hotel: <http://www.hotels.org#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org#>"
				+ "SELECT ?Hotel ?g " + "WHERE " + "{ " + "?Hotel rdf:type hotel:Hotel ." + "GRAPH ?x { "
				+ " ?Hotel f:type (hotel:quality hotel:Good ?g) }" + "}";

		String hashtag = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX twt: <http://www.twitter.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?s ?t ?rt ?o " + "WHERE { ?s rdf:type twt:tweet ."
				+ "?s twt:text ?t  . " + "?s f:type (twt:retweet__count twt:high ?rt) ."
				+ "?s f:type (twt:opinion twt:positive ?o) ." + "FILTER(f:OR_GOD(?rt,?o) >0.5)}";

		String UserShow = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX twt: <http://www.twitter.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?s ?fc ?fdc " + "WHERE { ?s rdf:type twt:user ."
				+ "?s twt:followers__count ?fc ." + "?s twt:friends__count ?fdc}";

		String User_Time_Line = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX twt: <http://www.twitter.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?s ?t ?o " + "WHERE { ?s rdf:type twt:tweet ."
				+ "?s twt:text ?t ." + "?s f:type (twt:opinion twt:positive ?o) ." + "FILTER(?o>0.5)}";

		String Retweeters = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX twt: <http://www.twitter.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?s ?id " + "WHERE { ?s twt:ids ?ids . ?ids twt:_ ?id }";

		String UserID = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX fsq: <http://www.foursquare.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?s ?hc " + "WHERE { ?s rdf:type fsq:user . "
				+ "?s fsq:firstName ?hc }";
		String User_Friends = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX fsq: <http://www.foursquare.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?s ?n "
				+ "WHERE { ?s fsq:friends ?f . ?f fsq:firstName ?n}";

		String User_Tips = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX fsq: <http://www.foursquare.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?u ?tt ?r " + "WHERE { ?u fsq:tips ?s . "
				+ "?s fsq:text ?tt . " + "?s f:type (fsq:opinion fsq:positive ?r)  . " + "FILTER(?r>0.5) }";

		String VenueID = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX fsq: <http://www.foursquare.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?s ?r ?rs ?b " + "WHERE { ?s rdf:type fsq:venue . "
				+ "?s fsq:rating ?r . ?s fsq:ratingSignals ?rs }";

		String Venue_Tips = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX fsq: <http://www.foursquare.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?s ?tt ?r " + "WHERE {" + "?s fsq:text ?tt . "
				+ "?s f:type (fsq:opinion fsq:positive ?r) . " + "FILTER(?r >0.5)}";

		String Venue_SimilarVenues = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX fsq: <http://www.foursquare.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?s ?n " + "WHERE { ?s fsq:similar ?t . ?t fsq:name ?n}";
		String Near = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX fsq: <http://www.foursquare.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?s ?n " + "WHERE { ?s fsq:near ?t . ?t fsq:name ?n}";

		String MovieID = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX tmdb: <http://www.tmdb.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?s ?r "
				+ "WHERE { ?s f:type (tmdb:categories tmdb:Action ?r)}";

		String Credits = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX tmdb: <http://www.tmdb.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?s ?n ?v " + "WHERE {?s tmdb:cast ?l  ."
				+ " ?l  tmdb:name ?n ." + " ?l  f:type (tmdb:relevance tmdb:high ?v) . " + " FILTER(?v > 0.25)}";

		String Reviews = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX tmdb: <http://www.tmdb.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?s ?tt ?r "
				+ "WHERE { ?s f:type (tmdb:opinion tmdb:positive ?r) . " + "?s tmdb:content ?tt }";

		String Now_Playing = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX tmdb: <http://www.tmdb.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?s ?n ?vc ?va " + "WHERE { ?s rdf:type tmdb:movie ."
				+ " ?s tmdb:original__title ?n ." + "?s f:type (tmdb:vote__count tmdb:high ?vc) ."
				+ " ?s f:type (tmdb:vote__average tmdb:high ?va) ." + " FILTER(f:MIN(?vc,?va) > 0.7) }";

		String PersonID = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX tmdb: <http://www.tmdb.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?s ?n ?p " + "WHERE { ?s rdf:type tmdb:person . "
				+ "?s tmdb:name ?n . ?s f:type (tmdb:popularity tmdb:high ?p)}";

		String Search = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "PREFIX tmdb: <http://www.tmdb.org#>"
				+ "PREFIX f: <http://www.fuzzy.org#>" + "SELECT ?s ?n ?va ?vc " + "WHERE { ?s rdf:type tmdb:movie ."
				+ " ?s tmdb:original__title ?n . " + "?s f:type (tmdb:vote__average  tmdb:high  ?va) ."
				+ "?s f:type (tmdb:vote__count tmdb:high ?vc) ." + " FILTER(f:MAX(?va,?vc) > 0.7)}";

		org.apache.log4j.BasicConfigurator.configure(new NullAppender());
		// String s = fsaSPARQL(hotels, fuzzy8);
		String s = FSAtoSPARQL(fuzzy11);
		System.out.println(s);

	};
};
