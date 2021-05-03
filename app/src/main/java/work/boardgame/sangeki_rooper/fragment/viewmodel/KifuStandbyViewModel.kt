package work.boardgame.sangeki_rooper.fragment.viewmodel

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.lifecycle.ViewModel

class KifuStandbyViewModel() : ViewModel(), Parcelable {
    var rootView: View? = null
    var tragedySetName:String? = null
    var loopCount:Int = 0
    var dayCount:Int = 0

    constructor(parcel: Parcel) : this() {
        tragedySetName = parcel.readString()
        loopCount = parcel.readInt()
        dayCount = parcel.readInt()
    }
    fun copyFromParcel(savedData: KifuStandbyViewModel) {
        tragedySetName = savedData.tragedySetName
        loopCount = savedData.loopCount
        dayCount = savedData.dayCount
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(tragedySetName)
        parcel.writeInt(loopCount)
        parcel.writeInt(dayCount)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<KifuStandbyViewModel> {
        override fun createFromParcel(parcel: Parcel): KifuStandbyViewModel = KifuStandbyViewModel(parcel)
        override fun newArray(size: Int): Array<KifuStandbyViewModel?> = arrayOfNulls(size)
    }
}