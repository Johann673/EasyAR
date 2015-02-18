# EasyAR

EasyAR est une bibliothèque Android qui permet de faciliter l'intégration de composants en réalité augmentée dans une application. Actuellement il est possible de faire :
  - Ajouter des POI (latitude, longitude)
  - Visualiser les POI avec la caméra
  - Indiquer une action à faire lors de clic sur un marqueur
  - Modifier l'image des marqueurs ou utiliser un layout personnalisé

## Démonstration
Un projet de test est disponible sur le dépot pour tester les fonctionnalités de base.
Screenshots en cours...

## Importer la bibliothèque
### Eclipse
En cours...
### Android Studio

## Utilisation
- Importer la bibliothèque
- Créer une activité de type FragmentActivity
```java
public class MainActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
```
- Un Fragment, le composant de réalité augmenté sera utilisé à travers un Fragment. Celui ci sera de type ARFragment (issue de la bibliothèque).
```java
public class SampleFragment extends ARFragment {
///...
}
```
- La vue activity_main.xml correspondante avec le fragment
```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <fragment
        android:id="@+id/arFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:name="fr.johann_web.ARSample.SampleFragment" />
</FrameLayout>
```
- Un layout (sample_overlay.xml) qui correspond aux marqueurs qui seront affichés devant la caméra pour les POI. L'exemple correspond à une image mais peut être remplacé par tout autre composant. Il est possible d'utiliser un layout différent pour chaque marqueur.
```xml
<ImageView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:src="@mipmap/ic_launcher"
    android:adjustViewBounds="false">
</ImageView>
```
- Une fois tous les fichiers créés, retournons dans le code du fragment pour y ajouter les POI. Pour cela, dans la méthode onCreateView, nous pouvons ajouter les marqueurs de cette façon :
```java
//Marqueurs un par un
addMarker(new ARMarker("marker1", 48.557856, 7.683909, R.layout.sample_overlay));
addMarker(new ARMarker("marker2", 48.487694, 7.719058, R.layout.sample_overlay));
//Ou directement avec une liste :
List<ARMarker> markers = new ArrayList<>();
markers.add(new ARMarker("marker3", 48.557856, 7.683909, R.layout.sample_overlay)):
markers.add(new ARMarker("marker4", 48.557856, 7.719058, R.layout.sample_overlay)):
addMarkers(markers);
```
L'objet ARMarker prend comme paramètres : Le nom du marqueur, la latitude, la longitude et le template associé. Il peut également prendre comme paramètre optionnel, un objet, qui permet de passer n'importe quelle données et de le récupérer lors du clic sur celui ci.
- Lors de l'implémentation du fragment ARFragment, la méthode onTapMarker doit être ajoutée. Celle ci permet d'éxecuter des actions lors d'un clic sur un marqueur. Elle se compose de telle sorte :
```java
@Override
protected void onTapMarker(ARMarker marker) {
    marker.getName(); //récupère le nom
    marker.getLocation(); //récupère les coordonnées du marqueur
    marker.getDistance(); //récupère la distance par rapport à notre position
    marker.getData(); //récupère l'objet personnel si il a été ajouté
}
```
- Il est également possible de récupérer des informations de positions diverses, comme par exemple lorsque la position GPS change, pour mettre à jours les marqueurs à afficher. Pour cela, le fragment doit implémenter l'interface ARView.OnChangeListener
```java
public class SampleFragment extends ARFragment implements ARView.OnChangeListener {
//...
}
```
Et ajouter le fragment en tant que listener dans le onCreateView
```java
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
    View view =  super.onCreateView(inflater, container, saved);
    setOnChangeListener(this);
    //...
}
```


## A faire

 - Ajouts de fonctionnalités
 - Optimisation de l'affichage
 - Résoudre les problèmes d'affichage lors de l'orientation
 - Encore beaucoup d'autres...

## Historique des versions
[0.1.0] : Version initiale

License
----

>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

> http://www.apache.org/licenses/LICENSE-2.0

>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
