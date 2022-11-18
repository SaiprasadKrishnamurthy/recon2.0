const functionReference = `
<h2 id="available-functions">Available functions</h2>
<p>You can do a lot of cool functions in your puesdo json that that help you randomize your test data.</p>
<p>random integer in range:</p>
<pre><code>{{integer(<span class="hljs-name"><span class="hljs-builtin-name">min</span></span>,max)}}
</code></pre><p>random float in range (with optional format):</p>
<pre><code>{{float(<span class="hljs-name"><span class="hljs-builtin-name">min</span></span>,max)}}
</code></pre><pre><code>{{float(<span class="hljs-name"><span class="hljs-builtin-name">min</span></span>,max,<span class="hljs-string">"%.2f"</span>)}}
</code></pre><p>random double in range (with optional format):</p>
<pre><code>{{double(<span class="hljs-name"><span class="hljs-builtin-name">min</span></span>,max)}}
</code></pre><pre><code>{{double(<span class="hljs-name"><span class="hljs-builtin-name">min</span></span>,max,<span class="hljs-string">"%.2f"</span>)}}
</code></pre><p>random long in range:</p>
<pre><code>{{long(<span class="hljs-name"><span class="hljs-builtin-name">min</span></span>,max)}}
</code></pre><p>uuid:</p>
<pre><code>{{uuid()}}
</code></pre><p>uuid without dashes:</p>
<pre><code>{{uuid(<span class="hljs-string">"false"</span>)}}
</code></pre><p>hex (16 bytes):</p>
<pre><code>{{hex()}}
</code></pre><p>hex (with byte size):</p>
<pre><code>{{hex(<span class="hljs-name">size</span>)}}
</code></pre><p>objectId (12 byte hex string):</p>
<pre><code>{{objectId()}}
</code></pre><p>random boolean:</p>
<pre><code>{{bool()}}
</code></pre><p>random boolean with given probability:</p>
<pre><code>{{bool(<span class="hljs-number">0.9</span>)}}
</code></pre><p>an incrementing index integer</p>
<pre><code>{{index()}
</code></pre><p>a named incrementing index integer</p>
<pre><code>{{index(<span class="hljs-string">"index-name"</span>)}
</code></pre><p>an incrementing index integer with a specific starting point</p>
<pre><code>{{index(<span class="hljs-number">78</span>)}
</code></pre><p>an incrementing index integer with a name and a specific starting point</p>
<pre><code>{{index(<span class="hljs-string">"index-name"</span>,<span class="hljs-number">78</span>)}
</code></pre><p>reset the default index</p>
<pre><code>{{resetIndex(<span class="hljs-string">"inner"</span>)}}
</code></pre><p>reset an index with name (more detailed example)</p>
<pre><code>[
  '{{repeat(<span class="hljs-number">3</span>)}}',
  {
    index: '{{index(<span class="hljs-string">"outer"</span>)}}',
    friends: [
      '{{repeat(<span class="hljs-number">3</span>)}}',
      {
        id: '{{index(<span class="hljs-string">"inner"</span>)}}',
        name: 'nameValue'{{resetIndex(<span class="hljs-string">"inner"</span>)}}
      }
    ],
}
]
</code></pre><p>lorem ipsum words:</p>
<pre><code>{{lorem(<span class="hljs-name"><span class="hljs-builtin-name">count</span></span>,<span class="hljs-string">"words"</span>)}}
</code></pre><p>lorem ipsum paragraphs:</p>
<pre><code>{{lorem(<span class="hljs-name"><span class="hljs-builtin-name">count</span></span>,<span class="hljs-string">"paragraphs"</span>)}}
</code></pre><p>random phone number:</p>
<pre><code>{{phone()}}
</code></pre><p>random gender (male or female):</p>
<pre><code>{{gender()}}
</code></pre><p>current date date (default format is: EEE, d MMM yyyy HH:mm:ss z):</p>
<pre><code>{{date()}}
</code></pre><p>current date with format:</p>
<pre><code>{{date(<span class="hljs-string">"java-simple-date-format"</span>)}}
</code></pre><p>random date between two dates with format (your input must be in this format dd-MM-yyyy HH:mm:ss):</p>
<pre><code>{{<span class="hljs-built_in">date</span>("begin-<span class="hljs-built_in">date</span>","end-<span class="hljs-built_in">date</span>","java-simple-<span class="hljs-built_in">date</span>-<span class="hljs-built_in">format</span>"}}
</code></pre><p>random date between two dates with default format (your input must be in this format EEE, d MMM yyyy HH:mm:ss z):</p>
<pre><code>{{date(<span class="hljs-string">"begin-date"</span>,<span class="hljs-string">"end-date"</span>}}
</code></pre><p>convert date format from one format to another:</p>
<pre><code>{{dateFormat(<span class="hljs-string">"06-16-1956 12:00:00"</span>, <span class="hljs-string">"from-simple-date-format"</span>, <span class="hljs-string">"to-simple-date-format"</span>)}}
</code></pre><p>current timestamp (milliseconds, between the current time and midnight, January 1, 1970 UTC):</p>
<pre><code>{{timestamp()}}
</code></pre><p>random timestamp (milliseconds since midnight, January 1, 1970 UTC) between two dates with default format (your input must be in this format EEE, d MMM yyyy HH:mm:ss z):</p>
<pre><code>{{timestamp(<span class="hljs-string">"begin-date"</span>,<span class="hljs-string">"end-date"</span>}}
</code></pre><p>random country:</p>
<pre><code>{{country()}}
</code></pre><p>add (or subtract) days to date with format:</p>
<pre><code>{{addDays(<span class="hljs-string">"dd-MM-yyyy HH:mm:ss"</span>, <span class="hljs-string">"03-11-2018 09:27:56"</span>, <span class="hljs-number">12</span>)}}
</code></pre><p>add (or subtract) days to date with default input format:</p>
<pre><code>{{addDays(<span class="hljs-string">"03-11-2018 09:27:56"</span>, <span class="hljs-number">12</span>)}}
</code></pre><p>add (or subtract) hours to date with format:</p>
<pre><code>{{addHours(<span class="hljs-string">"dd-MM-yyyy HH:mm:ss"</span>, <span class="hljs-string">"03-11-2018 09:27:56"</span>, <span class="hljs-number">12</span>)}}
</code></pre><p>add (or subtract) hours to date with default input format:</p>
<pre><code>{{addHours(<span class="hljs-string">"03-11-2018 09:27:56"</span>, <span class="hljs-number">12</span>)}}
</code></pre><p>add (or subtract) minutes to date with format:</p>
<pre><code>{{addMinutes(<span class="hljs-string">"dd-MM-yyyy HH:mm:ss"</span>, <span class="hljs-string">"03-11-2018 09:27:56"</span>, <span class="hljs-number">12</span>)}}
</code></pre><p>add (or subtract) minutes to date with default input format:</p>
<pre><code>{{addMinutes(<span class="hljs-string">"03-11-2018 09:27:56"</span>, <span class="hljs-number">12</span>)}}
</code></pre><p>add (or subtract) months to date with format:</p>
<pre><code>{{addMonths(<span class="hljs-string">"dd-MM-yyyy HH:mm:ss"</span>, <span class="hljs-string">"03-11-2018 09:27:56"</span>, <span class="hljs-number">12</span>)}}
</code></pre><p>add (or subtract) months to date with default input format:</p>
<pre><code>{{addMonths(<span class="hljs-string">"03-11-2018 09:27:56"</span>, <span class="hljs-number">12</span>)}}
</code></pre><p>add (or subtract) seconds to date with format:</p>
<pre><code>{{addSeconds(<span class="hljs-string">"dd-MM-yyyy HH:mm:ss"</span>, <span class="hljs-string">"03-11-2018 09:27:56"</span>, <span class="hljs-number">12</span>)}}
</code></pre><p>add (or subtract) seconds to date with default input format:</p>
<pre><code>{{addSeconds(<span class="hljs-string">"03-11-2018 09:27:56"</span>, <span class="hljs-number">12</span>)}}
</code></pre><p>add (or subtract) weeks to date with format:</p>
<pre><code>{{addWeeks(<span class="hljs-string">"dd-MM-yyyy HH:mm:ss"</span>, <span class="hljs-string">"03-11-2018 09:27:56"</span>, <span class="hljs-number">12</span>)}}
</code></pre><p>add (or subtract) weeks to date with default input format:</p>
<pre><code>{{addWeeks(<span class="hljs-string">"03-11-2018 09:27:56"</span>, <span class="hljs-number">12</span>)}}
</code></pre><p>add (or subtract) years to date with format:</p>
<pre><code>{{addYears(<span class="hljs-string">"dd-MM-yyyy HH:mm:ss"</span>, <span class="hljs-string">"03-11-2018 09:27:56"</span>, <span class="hljs-number">12</span>)}}
</code></pre><p>add (or subtract) years to date with default input format:</p>
<pre><code>{{addYears(<span class="hljs-string">"03-11-2018 09:27:56"</span>, <span class="hljs-number">12</span>)}}
</code></pre><p>convert date to timestamp with date format:</p>
<pre><code>{{toTimestamp(<span class="hljs-string">"03-11-2018 09:27:56"</span>, <span class="hljs-string">"dd-MM-yyyy HH:mm:ss"</span>)}}
</code></pre><p>convert date to timestamp with defaultFormat:</p>
<pre><code>{{toTimestamp(<span class="hljs-string">"Sat, 12 Feb 2022 21:01:21 GMT"</span>)}}
</code></pre><p>a json mapping with all country codes to mappings:</p>
<pre><code>{{countryList()}}
</code></pre><p>a json mapping with just specified country codes to mappings:</p>
<p>{&quot;IN&quot;:&quot;India&quot;,&quot;US&quot;:&quot;United States&quot;,&quot;UK&quot;:&quot;United Kingdom&quot;}</p>
<pre><code>{{countryList(<span class="hljs-string">"IN"</span>, <span class="hljs-string">"US"</span>, <span class="hljs-string">"UK"</span>)}}
</code></pre><p>random city:</p>
<pre><code>{{city()}}
</code></pre><p>random state:</p>
<pre><code>{{<span class="hljs-keyword">state</span>()}}
</code></pre><p>random company:</p>
<pre><code>{{company()}}
</code></pre><p>random lastname:</p>
<pre><code>{{lastName()}}
</code></pre><p>random first name:</p>
<pre><code>{{firstName()}}
</code></pre><p>random username based on first initial from random first name and lastname lowercased:</p>
<pre><code>{{username()}}
</code></pre><p>random email:</p>
<pre><code>{{email()}}
</code></pre><p>random email with domain:</p>
<pre><code>{{email(<span class="hljs-string">"mydomain.com"</span>)}}
</code></pre><p>random social security number:</p>
<pre><code>{{ssn()}}
</code></pre><p>random ipv4:</p>
<pre><code>{{ipv4()}}
</code></pre><p>random ipv6:</p>
<pre><code>{{ipv6()}}
</code></pre><p>random ipv6 (uppercase):</p>
<pre><code>{{ipv6(<span class="hljs-string">"upper"</span>)}}
</code></pre><p>random ipv6 (lowercase):</p>
<pre><code>{{ipv6(<span class="hljs-string">"lower"</span>)}}
</code></pre><p>concat (var arg):</p>
<pre><code>{{concat(<span class="hljs-string">"A"</span>,<span class="hljs-string">"B"</span>,<span class="hljs-string">"C"</span>,<span class="hljs-string">"D"</span>)}}
</code></pre><p>substring:</p>
<pre><code>{{substring(<span class="hljs-string">"word"</span>,<span class="hljs-number">3</span>)}}
</code></pre><pre><code>{{substring(<span class="hljs-string">"long word"</span>, <span class="hljs-number">1</span>, <span class="hljs-number">6</span>)}}
</code></pre><p>random item from list:</p>
<pre><code>{{random(<span class="hljs-string">"red"</span>,<span class="hljs-string">"yellow"</span>,<span class="hljs-string">"green"</span>)}}
</code></pre><p>random string with alphabetic characters (defaults to between 10 and 20 characters):</p>
<pre><code>{{alpha()}}
</code></pre><pre><code>{{alpha(<span class="hljs-name"><span class="hljs-builtin-name">min</span></span>,max)}}
</code></pre><pre><code>{{alpha(<span class="hljs-name">length</span>)}}
</code></pre><p>random string with alpha-numeric characters (defaults to between 10 and 20 characters):</p>
<pre><code>{{alphaNumeric()}}
</code></pre><pre><code>{{alphaNumeric(<span class="hljs-name"><span class="hljs-builtin-name">min</span></span>,max)}}
</code></pre><pre><code>{{alphaNumeric(<span class="hljs-name">length</span>)}}
</code></pre><p>lower case a string:</p>
<pre><code>{{toLowerCase(<span class="hljs-string">"red"</span>)}}
</code></pre><p>upper case a string:</p>
<pre><code>{{toUpperCase(<span class="hljs-string">"red"</span>)}}
</code></pre><p>put a value in the cache:</p>
<pre><code>{{put(<span class="hljs-string">"key"</span>, <span class="hljs-string">"VALUE"</span>}}
</code></pre><p>retrieve a value from the cache:</p>
<pre><code><span class="xml"></span><span class="hljs-template-variable">{{<span class="hljs-built_in">get</span>(<span class="hljs-string">"key"</span>}}</span><span class="xml"></span>
</code></pre><p>regexify (defaults to en-US local)... build string based on regex:</p>
<pre><code>{{regexify(<span class="hljs-string">"[a-z1-9]{10}"</span>)}}
</code></pre><p>regexify with local:</p>
<pre><code>{{regexify(<span class="hljs-string">"en-GB"</span>, <span class="hljs-string">"[a-z1-9]{10}"</span>)}}
</code></pre><h2 id="escape-braces">Escape braces</h2>
<p>If you want to escape braces from within a function use a single escape character as seen in the example below:</p>
<pre><code>{{concat(<span class="hljs-string">"\{"</span>, <span class="hljs-string">"test"</span>, <span class="hljs-string">"\}"</span>)}}
</code></pre><h2 id="xml-support">XML support</h2>
<pre><code><span class="xml"><span class="php"><span class="hljs-meta">&lt;?</span>xml version=<span class="hljs-string">"1.0"</span> encoding=<span class="hljs-string">"UTF-8"</span><span class="hljs-meta">?&gt;</span></span>
<span class="hljs-tag">&lt;<span class="hljs-name">root</span>&gt;</span>
  '</span><span class="hljs-template-variable">{{repeat(2)}}</span><span class="xml">',
  <span class="hljs-tag">&lt;<span class="hljs-name">element</span>&gt;</span>
    <span class="hljs-tag">&lt;<span class="hljs-name">id</span>&gt;</span></span><span class="hljs-template-variable">{{guid()}}</span><span class="xml"><span class="hljs-tag">&lt;/<span class="hljs-name">id</span>&gt;</span>
    <span class="hljs-tag">&lt;<span class="hljs-name">name</span>&gt;</span></span><span class="hljs-template-variable">{{firstName()}}</span><span class="xml"><span class="hljs-tag">&lt;/<span class="hljs-name">name</span>&gt;</span>
    <span class="hljs-tag">&lt;<span class="hljs-name">index</span>&gt;</span></span><span class="hljs-template-variable">{{lastName()}}</span><span class="xml"><span class="hljs-tag">&lt;/<span class="hljs-name">index</span>&gt;</span>
  <span class="hljs-tag">&lt;/<span class="hljs-name">element</span>&gt;</span>

<span class="hljs-tag">&lt;<span class="hljs-name">tags</span>&gt;</span>
      '</span><span class="hljs-template-variable">{{repeat(7)}}</span><span class="xml">',
      </span><span class="hljs-template-variable">{{lorem(1, <span class="hljs-string">"words"</span>)}}</span><span class="xml">
<span class="hljs-tag">&lt;/<span class="hljs-name">tags</span>&gt;</span>
<span class="hljs-tag">&lt;<span class="hljs-name">friends</span>&gt;</span>
      '</span><span class="hljs-template-variable">{{repeat(3)}}</span><span class="xml">',
      <span class="hljs-tag">&lt;<span class="hljs-name">friend</span>&gt;</span>
        <span class="hljs-tag">&lt;<span class="hljs-name">id</span>&gt;</span></span><span class="hljs-template-variable">{{index()}}</span><span class="xml"><span class="hljs-tag">&lt;/<span class="hljs-name">id</span>&gt;</span>
        <span class="hljs-tag">&lt;<span class="hljs-name">name</span>&gt;</span></span><span class="hljs-template-variable">{{firstName()}}</span><span class="xml"> </span><span class="hljs-template-variable">{{surname()}}</span><span class="xml"><span class="hljs-tag">&lt;/<span class="hljs-name">name</span>&gt;</span>
    <span class="hljs-tag">&lt;/<span class="hljs-name">friend</span>&gt;</span>
<span class="hljs-tag">&lt;/<span class="hljs-name">friends</span>&gt;</span>
<span class="hljs-tag">&lt;/<span class="hljs-name">root</span>&gt;</span></span>
</code></pre><h2 id="nesting-functions">Nesting functions</h2>
<p>jason-data-generator supports nesting functions as well.</p>
<p>For example, if you wanted to create results that looked like dollar amounts you could do something like:</p>
<pre><code>{{concat(<span class="hljs-string">"$"</span>,float(<span class="hljs-number">0.90310</span>, <span class="hljs-number">5.3421</span>, <span class="hljs-string">"%.2f"</span>))}}
</code></pre><p>or something like this if you wanted a capitalized F or M:</p>
<pre><code>{{toUpperCase(<span class="hljs-name">substring</span>(<span class="hljs-name">gender</span>(),<span class="hljs-number">0</span>,<span class="hljs-number">1</span>))}}
</code></pre><h2 id="use-of-put-and-get-functions">Use of put and get functions</h2>
<p>The get and put functions can be used to use previously-used values later on in the document.  The put function returns the value passed into it.  See the following example.</p>
<pre><code>{
  <span class="hljs-attr">"firstName"</span>: <span class="hljs-string">"{{put("</span>firstName<span class="hljs-string">", firstName())}}"</span>,
  <span class="hljs-attr">"lastName"</span>: <span class="hljs-string">"{{put("</span>lastName<span class="hljs-string">", lastName())}}"</span>,
  <span class="hljs-attr">"email"</span>: <span class="hljs-string">"{{get("</span>firstName<span class="hljs-string">")}}.{{get("</span>lastName<span class="hljs-string">")}}@mydomain.com"</span>
}
</code></pre><p>produces</p>
<pre><code>{
  <span class="hljs-attr">"firstName"</span>: <span class="hljs-string">"Eve"</span>,
  <span class="hljs-attr">"lastName"</span>: <span class="hljs-string">"Acosta"</span>,
  <span class="hljs-attr">"email"</span>: <span class="hljs-string">"Eve.Acosta@mydomain.com"</span>
}
</code></pre><h2 id="use-date-add-functions">Use date add functions</h2>
<p>The date add functions can be used in conjuction with the get and put functions to create new date values by adding (or subtracting) </p>
<pre><code>{
    <span class="hljs-attr">"day1"</span>: <span class="hljs-string">"{{put("</span>date<span class="hljs-string">", date("</span>dd-MM-yyyy HH:mm:ss<span class="hljs-string">"))}}"</span>,
    <span class="hljs-attr">"day2"</span>: <span class="hljs-string">"{{addDays(get("</span>date<span class="hljs-string">"), 12)}}"</span>
}
</code></pre><p>produces</p>
<pre><code>{
    <span class="hljs-attr">"day1"</span>: <span class="hljs-string">"03-11-2018 09:27:56"</span>,
    <span class="hljs-attr">"day2"</span>: <span class="hljs-string">"15-11-2018 09:27:56"</span>
}
</code></pre><h2 id="creating-custom-functions">Creating Custom Functions</h2>
<p>You can also create new functions if you create the classes and register the function with the function registry.</p>
<p>-When you create Functions you must annotate the class with the @Function annotation and you must specify one or more names for the function.
-Use the @FunctionInvocation annotation to indicate the method that will be executed when the function is called.  The arguments of the function must be strings (or a Vararg String argument) and the method must return a string.</p>
<pre><code>package my.package;

<span class="hljs-keyword">import</span> com.github.vincentrussell.json.datagenerator.functions.<span class="hljs-keyword">Function</span>;
<span class="hljs-keyword">import</span> com.github.vincentrussell.json.datagenerator.functions.FunctionInvocation;

<span class="hljs-keyword">import</span> java.util.Random;

@<span class="hljs-function"><span class="hljs-keyword">Function</span><span class="hljs-params">(name = "new-function")</span></span>
<span class="hljs-keyword">public</span> <span class="hljs-keyword">class</span> NewFunction {

    <span class="hljs-keyword">private</span> static <span class="hljs-keyword">final</span> Random RANDOM = new Random();

    @FunctionInvocation
    <span class="hljs-keyword">public</span> String getRandomInteger(String <span class="hljs-built_in">min</span>, String <span class="hljs-built_in">max</span>) {
        <span class="hljs-keyword">return</span> getRandomInteger(<span class="hljs-keyword">Integer</span>.parseInt(<span class="hljs-built_in">min</span>), <span class="hljs-keyword">Integer</span>.parseInt(<span class="hljs-built_in">max</span>));
    }

    <span class="hljs-keyword">private</span> String getRandomInteger(<span class="hljs-keyword">Integer</span> <span class="hljs-built_in">min</span>, <span class="hljs-keyword">Integer</span> <span class="hljs-built_in">max</span>) {
        <span class="hljs-built_in">int</span> randomNumber = RANDOM.nextInt(<span class="hljs-built_in">max</span> - <span class="hljs-built_in">min</span>) + <span class="hljs-built_in">min</span>;
        <span class="hljs-keyword">return</span> <span class="hljs-keyword">Integer</span>.toString(randomNumber);
    }
}
</code></pre><p>then you can put the jar that you have created on the classpath with the the standalone jar (-f registers one or more classes with the Function Registry):</p>
<pre><code>java -cp json-data-generator-<span class="hljs-number">1.14</span>-standalone<span class="hljs-selector-class">.jar</span>:yourfunctions<span class="hljs-selector-class">.jar</span> com<span class="hljs-selector-class">.github</span><span class="hljs-selector-class">.vincentrussell</span><span class="hljs-selector-class">.json</span><span class="hljs-selector-class">.datagenerator</span><span class="hljs-selector-class">.CLIMain</span> -s source<span class="hljs-selector-class">.json</span> -d destination<span class="hljs-selector-class">.json</span> -f my<span class="hljs-selector-class">.package</span><span class="hljs-selector-class">.NewFunction</span>
</code></pre><p>Or you add json-data-generator as a dependency to your application you can simply add your function to the registry like this:</p>
<pre><code>FunctionRegistry <span class="hljs-function"><span class="hljs-keyword">function</span><span class="hljs-title">Registry</span> = <span class="hljs-title">new</span> <span class="hljs-title">FunctionRegistry</span>(<span class="hljs-params"></span>)</span>; 
<span class="hljs-function"><span class="hljs-keyword">function</span><span class="hljs-title">Registry</span>.<span class="hljs-title">registerClass</span>(<span class="hljs-params">Customer.<span class="hljs-keyword">class</span></span>)</span>;
JsonDataGenerator jsonDataGenerator = <span class="hljs-keyword">new</span> JsonDataGeneratorImpl(<span class="hljs-function"><span class="hljs-keyword">function</span><span class="hljs-title">Registry</span>)</span>;
</code></pre>`;

export class DataService {
  getFunctionReference() {
    return functionReference;
  }
}
