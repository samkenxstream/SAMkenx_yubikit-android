package com.yubico.yubikit.android.app.ui.management

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.activityViewModels
import com.yubico.yubikit.android.app.R
import com.yubico.yubikit.android.app.ui.YubiKeyFragment
import com.yubico.yubikit.core.Transport
import com.yubico.yubikit.management.Capability
import com.yubico.yubikit.management.DeviceConfig
import com.yubico.yubikit.management.ManagementSession
import kotlinx.android.synthetic.main.fragment_management.*

class ManagementFragment : YubiKeyFragment<ManagementSession, ManagementViewModel>() {
    override val viewModel: ManagementViewModel by activityViewModels()

    private val checkboxIds = mapOf(
            (Transport.USB to Capability.OTP) to R.id.checkbox_usb_otp,
            (Transport.USB to Capability.U2F) to R.id.checkbox_usb_u2f,
            (Transport.USB to Capability.PIV) to R.id.checkbox_usb_piv,
            (Transport.USB to Capability.OATH) to R.id.checkbox_usb_oath,
            (Transport.USB to Capability.OPENPGP) to R.id.checkbox_usb_pgp,
            (Transport.USB to Capability.FIDO2) to R.id.checkbox_usb_fido2,

            (Transport.NFC to Capability.OTP) to R.id.checkbox_nfc_otp,
            (Transport.NFC to Capability.U2F) to R.id.checkbox_nfc_u2f,
            (Transport.NFC to Capability.PIV) to R.id.checkbox_nfc_piv,
            (Transport.NFC to Capability.OATH) to R.id.checkbox_nfc_oath,
            (Transport.NFC to Capability.OPENPGP) to R.id.checkbox_nfc_pgp,
            (Transport.NFC to Capability.FIDO2) to R.id.checkbox_nfc_fido2
    )

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_management, container, false)
    }

    override fun onStop() {
        viewModel.releaseYubiKey()
        super.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        application_table.visibility = View.GONE
        save.visibility = View.GONE

        viewModel.deviceInfo.observe(viewLifecycleOwner, {
            if (it != null) {
                info.text = "Device type: ${it.formFactor.name} \nFirmware: ${it.version} \nSerial: ${it.serial}"
                checkboxIds.forEach { (transport, app), id ->
                    view.findViewById<CheckBox>(id).let { checkbox ->
                        if (it.getSupportedApplications(transport) and app.bit != 0) {
                            checkbox.isChecked = (it.config.getEnabledApplications(transport) ?: 0) and app.bit != 0
                            checkbox.visibility = View.VISIBLE
                        } else {
                            checkbox.visibility = View.GONE
                        }
                    }
                }
                application_table.visibility = View.VISIBLE
                save.visibility = View.VISIBLE
                empty_view.visibility = View.GONE
            } else {
                empty_view.visibility = View.VISIBLE
                application_table.visibility = View.GONE
                save.visibility = View.GONE
            }
        })

        save.setOnClickListener {
            viewModel.pendingAction.value = {
                updateDeviceConfig(DeviceConfig.Builder().apply {
                    Transport.values().forEach { transport ->
                        enabledApplications(transport, checkboxIds.filter {
                            it.key.first == transport && view.findViewById<CheckBox>(it.value).isChecked
                        }.map {
                            it.key.second.bit  // Application bit
                        }.sum())
                    }
                }.build(), true, null, null)

                "Configuration updated"
            }
        }
    }
}