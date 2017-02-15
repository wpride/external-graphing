package external.commcare.org.graphtools;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Demo activity demonstrating how to read graph data from CommCare mobile UCR's
 * and display in a graphing library
 */
public class MainActivity extends AppCompatActivity {

    // Mobile UCR data is always in this fixture
    private static final String REPORT_INSTANCE_ID = "commcare:reports";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            selectFixture(REPORT_INSTANCE_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getReportXml(String fixtureId) {
        Cursor c = this.managedQuery(Uri.parse("content://org.commcare.dalvik.fixture/fixturedb/" + fixtureId), null, null, null, null);
        if (!c.moveToFirst()) {
            return null;
        }
        return c.getString(c.getColumnIndex("content"));
    }

    private Document getXmlDom(String reportXml) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        return dBuilder.parse(new InputSource(new StringReader(reportXml)));
    }

    private void selectFixture(String fixtureId) throws Exception {

        String reportXml = getReportXml(fixtureId);
        Document document = getXmlDom(reportXml);
        renderDocumentToGraph(document);
    }

    /**
     * Render our XML document into the GraphView. We are assuming an XML structure such as:
     *
     *       <report id="3d7782e232135e0bc1d06233cda04642c9e67d0e">
                <filters/>
                <rows>
                    <row index="0" is_total_row="False">
                        <column id="office_559ce81b">alumni</column>
                        <column id="total_burpees">2528</column>
                        <column id="total_goal">2700</column>
                    </row>
                    <row index="1" is_total_row="False">
                        ...
     * @param document the parsed XML document
     */
    private void renderDocumentToGraph(Document document) {
        // We've hard coded in this graph ID for the moment
        // TODO: Add a selector for this
        Element workerReport = document.getElementById("3d7782e232135e0bc1d06233cda04642c9e67d0e");
        NodeList rows = workerReport.getElementsByTagName("row");

        // We have our list of rows. Now iterate through it and pull labels and values
        ArrayList<DataPoint> dataPoints = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int count = 1;
        for (int i = 0; i < rows.getLength(); i++) {
            try {
                Node row = rows.item(i);
                NodeList columns = ((Element) row).getElementsByTagName("column");
                // Hacked in domain knowledge that the label is the first column, value the second
                String label = columns.item(0).getChildNodes().item(0).getNodeValue();
                String value = columns.item(1).getChildNodes().item(0).getNodeValue();
                dataPoints.add(new DataPoint(count, Integer.parseInt(value)));
                labels.add(label);
            } catch (Exception e) {
                System.out.println("Failed to add row " + i + " with e " + e);
            }
            count++;
        }

        DataPoint[] dataPointsArray = new DataPoint[dataPoints.size()];
        dataPoints.toArray(dataPointsArray);
        String[] labelsArray = new String[labels.size()];
        labels.toArray(labelsArray);

        // construct our graph using http://www.android-graphview.org/
        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dataPointsArray);
        GraphView graph = (GraphView) findViewById(R.id.graph);
        series.setSpacing(50); // 50% spacing between bars
        series.setAnimated(true);
        graph.addSeries(series);

        // setup labels
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(labelsArray);
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
    }
}
