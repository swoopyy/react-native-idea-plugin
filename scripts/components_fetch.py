import requests as req
from lxml import html
import pprint

url = 'https://facebook.github.io/react-native/docs/'

components = [
    'ActivityIndicator',
    'Button',
    'DatePickerIOS',
    'DrawerLayoutAndroid',
    'FlatList',
    'Image',
    'KeyboardAvoidingView',
    'ListView',
    'MaskedViewIOS',
    'Modal',
    'NavigatorIOS',
    'Picker',
    'PickerIOS',
    'ProgressBarAndroid',
    'ProgressViewIOS',
    'RefreshControl',
    'ScrollView',
    'SectionList',
    'SegmentedControlIOS',
    'Slider',
    'SnapshotViewIOS',
    'StatusBar',
    'Switch',
    'TabBarIOS',
    'TabBarIOS.Item',
    'Text',
    'TextInput',
    'ToolbarAndroid',
    'TouchableHighlight',
    'TouchableNativeFeedback',
    'TouchableOpacity',
    'TouchableWithoutFeedback',
    'View',
    'ViewPagerAndroid',
    'VirtualizedList',
    'WebView'
]

obj = {}
for component in components:
    result = req.get(url + component.lower() + '.html')
    tree = html.fromstring(result.text)
    h3 = tree.findall('.//h3')
    component_obj = {}
    h3_i = 0
    for el in h3:
        if el.findall("./a[@class='hash-link']") is None:
            continue
        else:
            code_el = el.findall("./code")
            if len(code_el) and '(' not in code_el[0].text:
                prop = code_el[0].text
                component_obj[prop] = {}
                props_attrs = el.xpath('.//following-sibling::p/following-sibling::table')
                if not props_attrs:
                    continue
                table = props_attrs[0].findall('.//tbody/tr/td')
                if len(table) < 2:
                    continue
                if table[0].text:
                    component_obj[prop]['type'] = table[0].text
                elif table[0].find('./a') is not None:
                    component_obj[prop]['type'] = table[0].find('./a').text
                else:
                    continue
                component_obj[prop]['required'] = table[1].text
                if len(table) > 2:
                    component_obj[prop]['platform'] = table[2].text
                component_obj['url'] = url + component.lower() + '.html'
    obj[component] = component_obj
    print(component)

import json
with open('data.json', 'w') as outfile:
    json.dump(obj, outfile)