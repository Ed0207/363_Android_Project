import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable


data class DBEventsInformation(
    var name: String ="",
    var startingdate: String="",
    var attendess: Long,
    var location: String="",
    var latLng: LatLng = LatLng(0.0,0.0),
    var information:String="",
    var creator:String="",
    var id:String="",
    var activitytypes:String="",
)