package external.commcare.org.graphtools;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            selectFixture("commcare:reports");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectFixture(String fixtureId) throws Exception {
        Cursor c = this.managedQuery(Uri.parse("content://org.commcare.dalvik.fixture/fixturedb/" + fixtureId), null, null, null, null);
        if (!c.moveToFirst()) {
            return;
        }
        String reportXml = c.getString(c.getColumnIndex("content"));
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new StringReader(reportXml)));
        Element workerReport = doc.getElementById("3d7782e232135e0bc1d06233cda04642c9e67d0e");
        NodeList rows = workerReport.getElementsByTagName("row");
        ArrayList<DataPoint> dataPoints = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int count = 1;
        for (int i = 0; i < rows.getLength(); i++) {
            try {
                Node row = rows.item(i);
                NodeList columns = ((Element) row).getElementsByTagName("column");
                String office = columns.item(0).getChildNodes().item(0).getNodeValue();
                String burpeeCount = columns.item(1).getChildNodes().item(0).getNodeValue();
                dataPoints.add(new DataPoint(count, Integer.parseInt(burpeeCount)));
                labels.add(office);
                count++;
            } catch (Exception e) {
                System.out.println("Failed to add row " + i + " with e " + e);
            }
        }
        DataPoint[] dataPointsArray = new DataPoint[dataPoints.size()];
        dataPoints.toArray(dataPointsArray);
        String[] labelsArray = new String[labels.size()];
        labels.toArray(labelsArray);

        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dataPointsArray);
        GraphView graph = (GraphView) findViewById(R.id.graph);
        series.setSpacing(50); // 50% spacing between bars
        series.setAnimated(true);
        graph.addSeries(series);

        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(6d);
        graph.getViewport().setXAxisBoundsManual(true);

        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(labelsArray);
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
    }
}
