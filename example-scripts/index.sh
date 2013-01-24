curl http://localhost:8082/solr/update -H "Content-Type: text/xml" --data-binary \
    '<add><doc>
            <field name="id">1</field>
            <field name="text">On the Shortness of Life</field>
            <field name="author">Seneca</field>
    </doc> 
    <doc>
            <field name="id">2</field>
            <field name="text">What I Talk About When I Talk About Running</field>
            <field name="author">Haruki Murakami</field>
    </doc> 
    <doc>
            <field name="id">3</field>
            <field name="text">The Dude and the Zen Master</field>
            <field name="author">Jeff "The Dude" Bridges</field>
    </doc>
    </add>'
curl http://localhost:8082/solr/update -H "Content-Type: text/xml" --data-binary '<commit />'
