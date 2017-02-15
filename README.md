# external-graphing

Demonstrates how to use mobile reporting data from CommCare in an external application.

This application assumes that you:
* Have CommCare installed on your Android device
* Have installed a CommCare application with mobile UCR turned on
* Have logged into CommCare as a user with access to those reports

Mobile reports are sent to CommCare as XML data formatted such as:

"""
      <report id="3d7782e232135e0bc1d06233cda04642c9e67d0e" report_id="2f8b875ecdd72e1cea9e34492d73913f">
        <filters/>
        <rows>
          <row index="0" is_total_row="False">
            <column id="office_559ce81b">alumni</column>
            <column id="total_burpees">2528</column>
            <column id="total_goal">2700</column>
          </row>
          <row index="1" is_total_row="False">
            <column id="office_559ce81b">dsa</column>
            <column id="total_burpees">4848</column>
            <column id="total_goal">5897</column>
          </row>
          ...
"""

These rows and columns containing labels, values and other configuration data are parsed by CommCare into graphs using d3

This demo applciation accesses the same data via the use of Android ContentProviders and displays them using http://www.android-graphview.org/
