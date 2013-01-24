package de.fhopf.solr.termqueryfacet;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.request.SolrQueryRequest;
import org.junit.BeforeClass;
import org.junit.Test;

import org.junit.Before;

public class TermQueryFacettingTest extends SolrTestCaseJ4 {

    @BeforeClass
    public static void initTestCore() throws Exception {
        SolrTestCaseJ4.initCore("solrhome/collection1/conf/solrconfig.xml", "solrhome/collection1/conf/schema.xml", "solrhome/");
    }

    @Before
    public void setUpIndex() {
        super.clearIndex();
        assertU(adoc("id", "1", "text", "On the Shortness of Life", "author", "Seneca"));
        assertU(adoc("id", "2", "text", "What I Talk About When I Talk About Running", "author", "Haruki Murakami"));
        assertU(adoc("id", "3", "text", "The Dude and the Zen Master", "author", "Jeff \"The Dude\" Bridges"));
        assertU(commit());

    }
    
    private static SolrQueryRequest facetRequest(String filter) {
        return req("qt", "/select", "fq", filter);
    }

    @Test
    public void findAll() {
        assertQ(facetRequest(""),
                "//result[@numFound='3']",
                "count(//lst[@name='facet_fields']/lst[@name='author']/int)='3'");
    }
    
    @Test
    public void filterOnSingleToken() {
        // TermQuery author:Seneca
        assertQ(facetRequest("author:Seneca"),
                "//result[@numFound='1']",
                "count(//lst[@name='facet_fields']/lst[@name='author']/int)='1'");
    } 
    
    @Test
    public void noResultForFilteringWithWhitespace() {
        // parsed to BooleanQuery author:Haruki text:murakami
        assertQ(facetRequest("author:Haruki Murakami"),
                "//result[@numFound='0']",
                "count(//lst[@name='facet_fields']/lst[@name='author']/int)='0'");
    } 
    
    @Test
    public void resultWhenEscapingWhitespace() {
        // parsed to TermQuery author:"Haruki Murakami"
        assertQ(facetRequest("author:\"Haruki Murakami\""),
                "//result[@numFound='1']",
                "count(//lst[@name='facet_fields']/lst[@name='author']/int)='1'");
    } 
    
    @Test
    public void searchingAlsoOnDefaultFieldWhenSearchingWithQuotes() {
        // this is a confusing case: you might expect that there are no hits
        // but as this string is parsed to a BooleanQuery author:Jeff text:"the dude" text:bridges
        // this matches "the dude" in the text field
        assertQ(facetRequest("author:Jeff \"The Dude\" Bridges"),
                "//result[@numFound='1']",
                "count(//lst[@name='facet_fields']/lst[@name='author']/int)='1'");
    } 
    
    @Test
    public void resultWhenEscapingQuotes() {
        // this is now parsed to a TermQuery "author:Jeff\ \"The\ Dude\"\ Bridges" which results in a hit in the author field
        assertQ(facetRequest("author:" + ClientUtils.escapeQueryChars("Jeff \"The Dude\" Bridges")),
                "//result[@numFound='1']",
                "count(//lst[@name='facet_fields']/lst[@name='author']/int)='1'");
    }
    
    @Test
    public void resultWhenUsingNestedQuery() {
        assertQ(facetRequest("{!term f=author v='Jeff \"The Dude\" Bridges'}"),
                "//result[@numFound='1']",
                "count(//lst[@name='facet_fields']/lst[@name='author']/int)='1'");
    }
    
    @Test
    public void resultWhenUsingLocalParam() {
        assertQ(req("qt", "/selectfiltered", "author", "Jeff \"The Dude\" Bridges"), 
                "//result[@numFound='1']",
                "count(//lst[@name='facet_fields']/lst[@name='author']/int)='1'");
    }
}
