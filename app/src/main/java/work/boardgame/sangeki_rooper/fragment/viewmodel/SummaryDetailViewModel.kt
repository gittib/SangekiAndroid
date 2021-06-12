package work.boardgame.sangeki_rooper.fragment.viewmodel

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.ViewModel

class SummaryDetailViewModel() : ViewModel(), Parcelable {
    var pdfAssetPath: String? = null

    constructor(parcel: Parcel) : this() {
        pdfAssetPath = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(pdfAssetPath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SummaryDetailViewModel> {
        override fun createFromParcel(parcel: Parcel): SummaryDetailViewModel {
            return SummaryDetailViewModel(parcel)
        }

        override fun newArray(size: Int): Array<SummaryDetailViewModel?> {
            return arrayOfNulls(size)
        }
    }
}