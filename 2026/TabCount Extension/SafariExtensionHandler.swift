//
//  SafariExtensionHandler.swift
//  TabCount Extension
//
//  Created by Gary Verhaegen on 2019-09-28.
//  Copyright © 2019 Sotenj. All rights reserved.
//

import SafariServices

class SafariExtensionHandler: SFSafariExtensionHandler {
    
    override func messageReceived(withName messageName: String, from page: SFSafariPage, userInfo: [String : Any]?) {
        // This method will be called when a content script provided by your extension calls safari.extension.dispatchMessage("message").
        page.getPropertiesWithCompletionHandler { properties in
            NSLog("The extension received a message (\(messageName)) from a script injected into (\(String(describing: properties?.url))) with userInfo (\(userInfo ?? [:]))")
        }
    }
    
    override func toolbarItemClicked(in window: SFSafariWindow) {
        NSLog("click")
    }
    
    override func validateToolbarItem(in _: SFSafariWindow, validationHandler: @escaping ((Bool, String) -> Void)) {
        var mutex = pthread_mutex_t()
        pthread_mutex_init(&mutex, nil)
        
        pthread_mutex_lock(&mutex)
        var count = 0
        var windows_count = 0
        var windows_done = -1
        pthread_mutex_unlock(&mutex)
        
        let q = DispatchQueue(label: "q", attributes: [DispatchQueue.Attributes.concurrent])
        let group = DispatchGroup()

        SFSafariApplication.getAllWindows { windows in
            pthread_mutex_lock(&mutex)
            windows_count = windows.count
            windows_done = 0
            pthread_mutex_unlock(&mutex)
            for window in windows {
                q.async(group: group, execute: {
                    window.getAllTabs { tabs in
                        q.async(group: group, execute: {
                            pthread_mutex_lock(&mutex)
                            count += tabs.count
                            windows_done += 1
                            pthread_mutex_unlock(&mutex)
                        })
                    }
                })
            }
        }
        q.async(group: group, execute: {
            var loop_count = 0
            loop: while loop_count < 10000 {
                loop_count += 1
                pthread_mutex_lock(&mutex)
                if windows_done == windows_count {
                    pthread_mutex_unlock(&mutex)
                    break loop
                } else {
                    pthread_mutex_unlock(&mutex)
                    Thread.sleep(forTimeInterval: 0.000001)
                }
            }
            validationHandler(true, "\(count)")
        })
    }
    
    override func popoverViewController() -> SFSafariExtensionViewController {
        return SafariExtensionViewController.shared
    }

}
